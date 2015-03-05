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
package com.codenvy.ide.ext.runner.client.manager;

import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.app.CurrentProject;
import com.codenvy.ide.api.parts.PartStackUIResources;
import com.codenvy.ide.ext.runner.client.RunnerLocalizationConstant;
import com.codenvy.ide.ext.runner.client.RunnerResources;
import com.codenvy.ide.ext.runner.client.inject.factories.WidgetFactory;
import com.codenvy.ide.ext.runner.client.manager.button.ButtonWidget;
import com.codenvy.ide.ext.runner.client.manager.info.MoreInfo;
import com.codenvy.ide.ext.runner.client.models.Runner;
import com.codenvy.ide.ext.runner.client.tabs.container.TabContainer;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.vectomatic.dom.svg.ui.SVGResource;

import static com.codenvy.ide.ext.runner.client.models.Runner.Status.FAILED;
import static com.codenvy.ide.ext.runner.client.models.Runner.Status.IN_QUEUE;
import static com.codenvy.ide.ext.runner.client.models.Runner.Status.STOPPED;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Andrienko Alexander
 */
@RunWith(GwtMockitoTestRunner.class)
public class RunnerManagerViewImplTest {

    private static final String TEXT                     = "some text";
    private static final String GWT_POPUP_STANDARD_STYLE = "gwt-PopupPanel";

    //mocks for constructor
    @Mock
    private PartStackUIResources       partStackUIResources;
    @Mock
    private RunnerResources            resources;
    @Mock
    private RunnerLocalizationConstant locale;
    @Mock
    private WidgetFactory              widgetFactory;
    @Mock
    private PopupPanel                 popupPanel;
    @Mock
    private ButtonWidget               buttonWidget;
    @Mock
    private AppContext                 appContext;

    @Mock
    private PartStackUIResources.PartStackCss css;
    @Mock
    private RunnerResources.RunnerCss         runnerCss;
    @Mock
    private MoreInfo                          moreInfoWidget;
    @Mock
    private SplitLayoutPanel                  splitLayoutPanel;
    @Mock
    private SVGResource                       imageRun;
    @Mock
    private ButtonWidget                      run;
    @Mock
    private SVGResource                       imageStop;
    @Mock
    private ButtonWidget                      stop;
    @Mock
    private Runner                            runner;
    @Mock
    private CurrentProject                    currentProject;
    @Mock
    private TabContainer                      containerPresenter;

    private RunnerManagerViewImpl view;

    @Before
    public void setUp() {
        when(partStackUIResources.partStackCss()).thenReturn(css);
        when(css.ideBasePartToolbar()).thenReturn(TEXT);
        when(partStackUIResources.minimize()).thenReturn(mock(SVGResource.class, RETURNS_DEEP_STUBS));

        when(resources.runnerCss()).thenReturn(runnerCss);
        when(runnerCss.opacityButton()).thenReturn(TEXT);

        when(resources.runAppImage()).thenReturn(imageRun);
        when(locale.tooltipRunButton()).thenReturn(TEXT);
        when(widgetFactory.createButton(TEXT, imageRun)).thenReturn(run);

        when(resources.stopButton()).thenReturn(imageStop);
        when(locale.tooltipStopButton()).thenReturn(TEXT);
        when(widgetFactory.createButton(TEXT, imageStop)).thenReturn(stop);

        when(locale.tooltipDockerButton()).thenReturn(TEXT);

        when(widgetFactory.createMoreInfo()).thenReturn(moreInfoWidget);
        when(locale.runnersPanelTitle()).thenReturn(TEXT);
        view = new RunnerManagerViewImpl(partStackUIResources, resources, locale, widgetFactory, appContext, popupPanel);
    }

    @Test
    public void shouldVerifyConstructor() {
        verify(widgetFactory).createMoreInfo();
        verify(locale).runnersPanelTitle();

        verify(popupPanel).removeStyleName(GWT_POPUP_STANDARD_STYLE);
        verify(popupPanel).add(moreInfoWidget);

        //set action delegate for testing handlers
        RunnerManagerView.ActionDelegate actionDelegate = mock(RunnerManagerView.ActionDelegate.class);
        view.setDelegate(actionDelegate);

        ArgumentCaptor<MouseOverHandler> mouseOverHandlerCaptor = ArgumentCaptor.forClass(MouseOverHandler.class);
        verify(view.moreInfoPanel).addDomHandler(mouseOverHandlerCaptor.capture(), eq(MouseOverEvent.getType()));
        MouseOverHandler mouseOverHandler = mouseOverHandlerCaptor.getValue();
        mouseOverHandler.onMouseOver(mock(MouseOverEvent.class));

        verify(view.image).addStyleName(TEXT);
        verify(actionDelegate).onMoreInfoBtnMouseOver();

        ArgumentCaptor<MouseOutHandler> mouseOutHandlerCaptor = ArgumentCaptor.forClass(MouseOutHandler.class);
        verify(view.moreInfoPanel).addDomHandler(mouseOutHandlerCaptor.capture(), eq(MouseOutEvent.getType()));
        MouseOutHandler mouseOutHandler = mouseOutHandlerCaptor.getValue();
        mouseOutHandler.onMouseOut(mock(MouseOutEvent.class));

        verify(resources, times(2)).runnerCss();
        verify(runnerCss, times(2)).opacityButton();
        verify(view.image).removeStyleName(TEXT);
        verify(popupPanel).hide();

        /* verify initialize button */
        //run button
        verify(widgetFactory).createButton(TEXT, imageRun);
        verifyButton(imageRun, run, view.runButtonPanel);
        verify(actionDelegate).onRunButtonClicked();
        verify(run).setEnable();

        //stop button
        verify(widgetFactory).createButton(TEXT, imageStop);
        verifyButton(imageStop, stop, view.otherButtonsPanel);
        verify(actionDelegate).onStopButtonClicked();
    }

    private void verifyButton(SVGResource imageResource, ButtonWidget btnWidget, FlowPanel buttonPanel) {
        ArgumentCaptor<ButtonWidget.ActionDelegate> btnCaptor = ArgumentCaptor.forClass(ButtonWidget.ActionDelegate.class);

        verify(widgetFactory).createButton(TEXT, imageResource);
        verify(btnWidget).setDelegate(btnCaptor.capture());
        verify(btnWidget).setDisable();
        verify(buttonPanel).add(btnWidget);

        ButtonWidget.ActionDelegate runButtonDelegate = btnCaptor.getValue();
        runButtonDelegate.onButtonClicked();
    }

    @Test
    public void allButtonsShouldBeDisabledWhenCurrentProjectIsNull() throws Exception {
        when(appContext.getCurrentProject()).thenReturn(null);

        view.update(runner);

        verify(run, times(2)).setDisable();
        verify(stop, times(2)).setDisable();
    }

    @Test
    public void shouldUpdateWhenRunnerInStatusInQueue() {
        reset(run, stop);
        when(runner.getStatus()).thenReturn(IN_QUEUE);
        when(appContext.getCurrentProject()).thenReturn(currentProject);

        view.update(runner);

        verifyEnableAllButton();
        verify(run).setDisable();
        verify(stop).setDisable();
    }

    @Test
    public void shouldUpdateWhenRunnerInStatusFailed() {
        reset(run, stop);
        when(runner.getStatus()).thenReturn(FAILED);
        when(appContext.getCurrentProject()).thenReturn(currentProject);

        view.update(runner);

        verifyEnableAllButton();
        verify(stop).setDisable();
    }

    @Test
    public void shouldUpdateWhenRunnerInStatusStopped() {
        reset(run, stop);
        when(runner.getStatus()).thenReturn(STOPPED);
        when(appContext.getCurrentProject()).thenReturn(currentProject);

        view.update(runner);

        verifyEnableAllButton();
        verify(stop).setDisable();
    }

    private void verifyEnableAllButton() {
        verify(run).setEnable();
        verify(stop).setEnable();
    }

    @Test
    public void shouldSetCorrectApplicationURl() {
        String url = "http://some/url";
        when(runnerCss.cursor()).thenReturn(TEXT);

        view.setApplicationURl(url);

        verify(view.appReference).removeStyleName(TEXT);
        verify(view.appReference).addStyleName(TEXT);
        verify(view.appReference).setText(url);
    }

    @Test
    public void shouldSetInCorrectApplicationURl() {
        String url = "some/url";
        when(runnerCss.cursor()).thenReturn(TEXT);

        view.setApplicationURl(url);

        verify(view.appReference).removeStyleName(TEXT);
        verify(view.appReference, never()).addStyleName(TEXT);
        verify(view.appReference).setText(url);
    }

    @Test
    public void shouldSetTimeOut() {
        view.setTimeout(TEXT);

        verify(view.timeout).setText(TEXT);
    }

    @Test
    public void shouldShowMoreInfoPopup() {
        when(view.timeout.getAbsoluteLeft()).thenReturn(150);
        when(view.timeout.getAbsoluteTop()).thenReturn(150);

        view.showMoreInfoPopup(runner);

        verify(moreInfoWidget).update(runner);
        verify(view.timeout).getAbsoluteLeft();
        verify(view.timeout).getAbsoluteTop();
        verify(popupPanel).setPopupPosition(50, 20);
        verify(popupPanel).show();
    }

    @Test
    public void shouldUpdateMoreInfoPopup() {
        view.updateMoreInfoPopup(runner);

        verify(moreInfoWidget).update(runner);
    }

    @Test
    public void shouldSetLeftPanel() {
        view.setLeftPanel(containerPresenter);

        verify(containerPresenter).go(view.leftTabsPanel);
    }

    @Test
    public void shouldSetRightPanel() {
        view.setRightPanel(containerPresenter);

        verify(containerPresenter).go(view.rightPanel);
    }

    @Test
    public void shouldHideOtherButtons() {
        view.hideOtherButtons();

        verify(view.otherButtonsPanel).setVisible(false);
    }

    @Test
    public void shouldShowOtherButtons() {
        view.showOtherButtons();

        verify(view.otherButtonsPanel).setVisible(true);
    }

    @Test
    public void runButtonShouldBeEnabled() throws Exception {
        reset(run);
        view.setEnableRunButton(true);

        verify(run).setEnable();
        verify(run, never()).setDisable();
    }

    @Test
    public void runButtonShouldBeDisabled() throws Exception {
        reset(run);
        view.setEnableRunButton(false);

        verify(run, never()).setEnable();
        verify(run).setDisable();
    }

    @Test
    public void stopButtonShouldBeEnabled() throws Exception {
        reset(stop);
        view.setEnableStopButton(true);

        verify(stop).setEnable();
        verify(stop, never()).setDisable();
    }

    @Test
    public void stopButtonShouldBeDisabled() throws Exception {
        reset(stop);
        view.setEnableStopButton(false);

        verify(stop, never()).setEnable();
        verify(stop).setDisable();
    }
}