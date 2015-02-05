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
package com.codenvy.ide.ext.runner.client.properties;

import com.codenvy.ide.api.mvp.View;
import com.codenvy.ide.ext.runner.client.properties.common.Boot;
import com.codenvy.ide.ext.runner.client.properties.common.RAM;
import com.codenvy.ide.ext.runner.client.properties.common.Scope;
import com.codenvy.ide.ext.runner.client.properties.common.Shutdown;
import com.google.inject.ImplementedBy;

import javax.annotation.Nonnull;

/**
 * The visual part of Properties panel that has an ability to show configuration of a runner.
 *
 * @author Andrey Plotnikov
 */
@ImplementedBy(PropertiesPanelViewImpl.class)
public interface PropertiesPanelView extends View<PropertiesPanelView.ActionDelegate> {

    /** @return content of Name field */
    @Nonnull
    String getName();

    /**
     * Changes content of Name field.
     *
     * @param name
     *         content that needs to be set
     */
    void setName(@Nonnull String name);

    /** @return chosen value of RAM field */
    @Nonnull
    RAM getMemorySize();

    /**
     * Select a given value into RAM field.
     *
     * @param size
     *         value that needs to be chosen
     */
    void selectMemory(@Nonnull RAM size);

    /** @return chosen value of Scope field */
    @Nonnull
    Scope getScope();

    /**
     * Select a given scope into Scope field.
     *
     * @param scope
     *         value that needs to be chosen
     */
    void selectScope(@Nonnull Scope scope);

    /** @return content of Type field */
    @Nonnull
    String getType();

    /**
     * Changes content of Type field.
     *
     * @param type
     *         content that needs to be set
     */
    void setType(@Nonnull String type);

    /** @return chosen value of Boot field */
    @Nonnull
    Boot getBoot();

    /**
     * Select a given value into Boot field.
     *
     * @param boot
     *         value that needs to be chosen
     */
    void selectBoot(@Nonnull Boot boot);

    /** @return chosen value of Shutdown field */
    @Nonnull
    Shutdown getShutdown();

    /**
     * Select a given value into Shutdown field.
     *
     * @param shutdown
     *         value that needs to be chosen
     */
    void selectShutdown(@Nonnull Shutdown shutdown);

    /** @return content of Editor text area */
    @Nonnull
    String getContent();

    /**
     * Changes content of Editor text area.
     *
     * @param content
     *         content that needs to be set
     */
    void setContent(@Nonnull String content);

    interface ActionDelegate {

        /** Performs some actions in response to user's changing some configuration. */
        void onConfigurationChanged();

        /** Performs some actions in response to user's clicking on the 'Save' button. */
        void onSaveButtonClicked();

        /** Performs some actions in response to user's clicking on the 'Delete' button. */
        void onDeleteButtonClicked();

        /** Performs some actions in response to user's clicking on the 'Cancel' button. */
        void onCancelButtonClicked();

    }

}