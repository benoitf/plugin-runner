/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.ext.runner.client.util;

import com.codenvy.ide.api.notification.Notification;
import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.ext.runner.client.RunnerLocalizationConstant;
import com.codenvy.ide.ext.runner.client.manager.RunnerManagerPresenter;
import com.codenvy.ide.ext.runner.client.manager.RunnerManagerView;
import com.codenvy.ide.ext.runner.client.models.Runner;
import com.codenvy.ide.ui.dialogs.ConfirmCallback;
import com.codenvy.ide.ui.dialogs.DialogFactory;
import com.codenvy.ide.ui.dialogs.message.MessageDialog;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.annotation.Nonnegative;

import static com.codenvy.ide.ext.runner.client.customrun.MemorySize.MEMORY_128;
import static com.codenvy.ide.ext.runner.client.models.Runner.Status.FAILED;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(DataProviderRunner.class)
public class RunnerUtilImplTest {

    private static final String SOME_TEXT = "someText";

    @Captor
    private ArgumentCaptor<Notification> notificationCaptor;

    @Mock
    private DialogFactory              dialogFactory;
    @Mock
    private RunnerLocalizationConstant locale;
    @Mock
    private RunnerManagerPresenter     presenter;
    @Mock
    private NotificationManager        notificationManager;
    @Mock
    private RunnerManagerView          view;
    @Mock
    private MessageDialog              messageDialog;
    @Mock
    private Runner                     runner;
    @Mock
    private Throwable                  exception;

    private RunnerUtil util;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(presenter.getView()).thenReturn(view);

        util = new RunnerUtilImpl(dialogFactory, locale, presenter, notificationManager);

        when(locale.titlesWarning()).thenReturn(SOME_TEXT);
        when(dialogFactory.createMessageDialog(anyString(), anyString(), any(ConfirmCallback.class))).thenReturn(messageDialog);
    }

    @Test
    public void constructorShouldBeDone() throws Exception {
        verify(presenter).getView();
    }

    @DataProvider
    public static Object[][] checkIsNonNegativeMemoryValue() {
        return new Object[][]{
                {-1, 1, 1},
                {1, -1, 1},
                {1, 1, -1}
        };
    }

    @Test
    @UseDataProvider("checkIsNonNegativeMemoryValue")
    public void runnerMemoryShouldBeAboveZero(@Nonnegative int totalMemory, @Nonnegative int usedMemory, @Nonnegative int availableMemory) {
        when(locale.messagesIncorrectValue()).thenReturn(SOME_TEXT);

        boolean isCorrect = util.isRunnerMemoryCorrect(totalMemory, usedMemory, availableMemory);

        verifyShowWarning();
        verify(locale).messagesIncorrectValue();

        assertThat(isCorrect, is(false));
    }

    private void verifyShowWarning() {
        verify(dialogFactory).createMessageDialog(SOME_TEXT, SOME_TEXT, null);
        verify(messageDialog).show();
    }

    @Test
    public void errorMessageShouldBeShownWhenMemoryNotMultiple128() throws Exception {
        when(locale.ramSizeMustBeMultipleOf(MEMORY_128.getValue())).thenReturn(SOME_TEXT);

        boolean isCorrect = util.isRunnerMemoryCorrect(125, 123, 125);

        verifyShowWarning();
        verify(locale).ramSizeMustBeMultipleOf(MEMORY_128.getValue());

        assertThat(isCorrect, is(false));
    }

    @Test
    public void errorMessageShouldBeShownWhenUsedMemoryMoreTotalMemory() throws Exception {
        when(locale.messagesTotalRamLessCustom(anyInt(), anyInt())).thenReturn(SOME_TEXT);

        boolean isCorrect = util.isRunnerMemoryCorrect(125, 128, 125);

        verifyShowWarning();
        verify(locale).messagesTotalRamLessCustom(128, 125);

        assertThat(isCorrect, is(false));
    }

    @Test
    public void errorMessageShouldBeShownWhenUsedMemoryMoreAvailableMemory() throws Exception {
        when(locale.messagesAvailableRamLessCustom(anyInt(), anyInt(), anyInt())).thenReturn(SOME_TEXT);

        boolean isCorrect = util.isRunnerMemoryCorrect(257, 256, 128);

        verifyShowWarning();
        verify(locale).messagesAvailableRamLessCustom(256, 257, 129);

        assertThat(isCorrect, is(false));
    }

    @Test
    public void memoryShouldBeCorrect() throws Exception {
        boolean isCorrect = util.isRunnerMemoryCorrect(256, 256, 256);

        assertThat(isCorrect, is(true));

        verify(dialogFactory, never()).createMessageDialog(anyString(), anyString(), any(ConfirmCallback.class));
    }

    @Test
    public void errorShouldBeShown() throws Exception {
        when(exception.getMessage()).thenReturn(null);

        util.showError(runner, SOME_TEXT, exception);

        verifyShowError();
        verify(view).printError(runner, SOME_TEXT);
    }

    private void verifyShowError() {
        verify(runner).setStatus(FAILED);
        verify(presenter).update(runner);

        verify(notificationManager).showNotification(notificationCaptor.capture());

        Notification notification = notificationCaptor.getValue();

        assertThat(notification.isError(), is(true));
        assertThat(notification.isFinished(), is(true));
        assertThat(notification.isImportant(), is(true));
    }

    @Test
    public void errorShouldBeShownWhenExceptionIsNotNull() throws Exception {
        when(exception.getMessage()).thenReturn(SOME_TEXT);

        util.showError(runner, SOME_TEXT, exception);

        verifyShowError();
        verify(view).printError(runner, SOME_TEXT + ": " + SOME_TEXT);
    }

}