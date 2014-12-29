/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.ext.runner.client.runneractions.impl;

import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.api.project.shared.dto.RunnerConfiguration;
import com.codenvy.api.project.shared.dto.RunnersDescriptor;
import com.codenvy.api.runner.dto.ResourcesDescriptor;
import com.codenvy.api.runner.gwt.client.RunnerServiceClient;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.app.CurrentProject;
import com.codenvy.ide.api.notification.Notification;
import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.ext.runner.client.RunnerLocalizationConstant;
import com.codenvy.ide.ext.runner.client.callbacks.AsyncCallbackFactory;
import com.codenvy.ide.ext.runner.client.callbacks.FailureCallback;
import com.codenvy.ide.ext.runner.client.callbacks.SuccessCallback;
import com.codenvy.ide.ext.runner.client.manager.RunnerManagerView;
import com.codenvy.ide.ext.runner.client.models.Runner;
import com.codenvy.ide.ext.runner.client.runneractions.RunnerAction;
import com.codenvy.ide.ext.runner.client.runneractions.impl.run.RunAction;
import com.codenvy.ide.ui.dialogs.ConfirmCallback;
import com.codenvy.ide.ui.dialogs.DialogFactory;
import com.codenvy.ide.ui.dialogs.confirm.ConfirmDialog;
import com.codenvy.ide.ui.dialogs.message.MessageDialog;
import com.google.inject.Inject;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.codenvy.ide.ext.runner.client.models.Runner.Status.FAILED;

/**
 * This action executes a request on the server side for getting resources of project. These resources are used for running a runner with
 * the custom memory size.
 *
 * @author Artem Zatsarynnyy
 * @author Andrey Parfonov
 * @author Roman Nikitenko
 * @author Valeriy Svydenko
 */
public class CheckRamAction implements RunnerAction {
    private final RunnerServiceClient        service;
    private final AppContext                 appContext;
    private final AsyncCallbackFactory       asyncCallbackFactory;
    private final DialogFactory              dialogFactory;
    private final RunnerLocalizationConstant constant;
    private final RunAction                  runAction;
    private final NotificationManager        notificationManager;
    private final Notification               notification;

    private RunnerConfiguration runnerConfiguration;
    private CurrentProject      project;
    private Runner              runner;
    private RunnerManagerView   view;

    @Inject
    public CheckRamAction(RunnerServiceClient service,
                          AppContext appContext,
                          DialogFactory dialogFactory,
                          NotificationManager notificationManager,
                          Notification notification,
                          RunnerManagerView view,
                          AsyncCallbackFactory asyncCallbackFactory,
                          RunnerLocalizationConstant constant,
                          RunAction runAction) {
        this.service = service;
        this.appContext = appContext;
        this.asyncCallbackFactory = asyncCallbackFactory;
        this.constant = constant;
        this.runAction = runAction;
        this.dialogFactory = dialogFactory;
        this.notificationManager = notificationManager;
        this.notification = notification;
        this.view = view;
    }

    /** {@inheritDoc} */
    @Override
    public void perform(@Nonnull Runner runner) {
        this.runner = runner;

        project = appContext.getCurrentProject();
        if (project == null) {
            return;
        }

        service.getResources(asyncCallbackFactory
                                     .build(ResourcesDescriptor.class, new SuccessCallback<ResourcesDescriptor>() {
                                                @Override
                                                public void onSuccess(ResourcesDescriptor resourcesDescriptor) {
                                                    checkRamAndRunProject(resourcesDescriptor);
                                                }
                                            },
                                            new FailureCallback() {
                                                @Override
                                                public void onFailure(@Nonnull Throwable reason) {
                                                    onFail(constant.getResourcesFailed(), reason);
                                                }
                                            }));
    }

    private void checkRamAndRunProject(@Nonnull ResourcesDescriptor resourcesDescriptor) {
        int totalMemory = Integer.valueOf(resourcesDescriptor.getTotalMemory());
        int usedMemory = Integer.valueOf(resourcesDescriptor.getUsedMemory());

        int overrideMemory = getOverrideMemory();
        int requiredMemory = runnerConfiguration != null ? runnerConfiguration.getRam() : 0;

        if (!isSufficientMemory(totalMemory, usedMemory, requiredMemory)) {
            runner.setAppLaunchStatus(false);
            return;
        }

        if (overrideMemory > 0) {
            if (!isOverrideMemoryCorrect(totalMemory, usedMemory, overrideMemory)) {
                runner.setAppLaunchStatus(false);

                return;
            }

            if (overrideMemory < requiredMemory) {
                runProjectWithRequiredMemory(requiredMemory, overrideMemory);

                return;
            }

            runner.setRAM(overrideMemory);

            runAction.perform(runner);
            return;
        }

        if (requiredMemory > 0) {
            runner.setRAM(requiredMemory);
        }

        /* Do not provide any value runnerMemorySize if:
        * - overrideMemory = 0
        * - requiredMemory = 0
        * - requiredMemory > workspaceMemory
        * - requiredMemory > availableMemory
        */
        runAction.perform(runner);
    }

    @Nonnegative
    private int getOverrideMemory() {
        ProjectDescriptor projectDescriptor = project.getProjectDescription();

        initializeRunnerConfiguration(projectDescriptor);

        return runner.getRAM();

        //TODO check is this code needs for us
                                                    /*else {
                                                        if (runners != null) {
                                                            runnerConfiguration = runners.getConfigs().get(runners.getDefault());
                                                        }
                                                        if (preferencesManager != null &&
                                                            preferencesManager.getValue(PREFS_RUNNER_RAM_SIZE_DEFAUL) !=
                                                            null) {
                                                            try {
                                                                overrideMemory =
                                                                        Integer.parseInt(preferencesManager.getValue(
                                                                                RunnerExtension.PREFS_RUNNER_RAM_SIZE_DEFAULT));
                                                            } catch (NumberFormatException e) {
                                                                //do nothing
                                                            }
                                                        }
                                                    }*/
    }

    private void initializeRunnerConfiguration(@Nonnull ProjectDescriptor projectDescriptor) {
        RunnersDescriptor runners = projectDescriptor.getRunners();

        if (runners == null) {
            return;
        }

        runnerConfiguration = runners.getConfigs().get(runner.getEnvironmentId());

        if (runnerConfiguration == null) {
            runnerConfiguration = runners.getConfigs().get(runners.getDefault());
        }
    }

    private void runProjectWithRequiredMemory(@Nonnegative final int requiredMemory, @Nonnegative int overrideMemory) {
        /*Offer the user to run an application with requiredMemory
        * If the user selects OK, then runnerMemory = requiredMemory
        * Else we should terminate the Runner process*/
        final ConfirmDialog confirmDialog = dialogFactory.createConfirmDialog(
                constant.titlesWarning(),
                constant.messagesOverrideMemory(), new ConfirmCallback() {
                    @Override
                    public void accepted() {
                        runner.setRAM(requiredMemory);

                        runAction.perform(runner);
                    }
                }, null);

        final MessageDialog messageDialog = dialogFactory.createMessageDialog(
                constant.titlesWarning(),
                constant.messagesOverrideLessRequiredMemory(overrideMemory, requiredMemory),
                new ConfirmCallback() {
                    @Override
                    public void accepted() {
                        confirmDialog.show();
                    }
                });

        messageDialog.show();
    }

    private void onFail(@Nonnull String message, @Nullable Throwable exception) {
        notification.setMessage(message);
        notificationManager.showNotification(notification);

        if (exception != null && exception.getMessage() != null) {
            view.printError(runner, message + ": " + exception.getMessage());
        } else {
            view.printError(runner, message);
        }

        runner.setAppLaunchStatus(false);
        runner.setStatus(FAILED);

        view.update(runner);
    }

    private boolean isSufficientMemory(@Nonnegative int totalMemory,
                                       @Nonnegative int usedMemory,
                                       @Nonnegative final int requiredMemory) {
        int availableMemory = totalMemory - usedMemory;
        if (totalMemory < requiredMemory) {
            showWarning(constant.messagesTotalLessRequiredMemory(totalMemory, requiredMemory));
            return false;
        }

        if (availableMemory < requiredMemory) {
            showWarning(constant.messagesAvailableLessRequiredMemory(totalMemory, usedMemory, requiredMemory));
            return false;
        }

        return true;
    }

    private boolean isOverrideMemoryCorrect(@Nonnegative int totalMemory,
                                            @Nonnegative int usedMemory,
                                            @Nonnegative final int overrideMemory) {
        int availableMemory = totalMemory - usedMemory;
        if (totalMemory < overrideMemory) {
            showWarning(constant.messagesTotalLessOverrideMemory(overrideMemory, totalMemory));
            return false;
        }

        if (availableMemory < overrideMemory) {
            showWarning(constant.messagesAvailableLessOverrideMemory(overrideMemory, totalMemory, usedMemory));
            return false;
        }

        return true;
    }

    private void showWarning(@Nonnull String warning) {
        dialogFactory.createMessageDialog(constant.titlesWarning(), warning, null).show();
    }

    /** {@inheritDoc} */
    @Override
    public void stop() {
        //do nothing
    }

}