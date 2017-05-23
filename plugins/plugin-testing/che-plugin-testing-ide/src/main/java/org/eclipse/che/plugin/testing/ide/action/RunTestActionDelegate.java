/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.testing.ide.action;

import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcPromise;
import org.eclipse.che.api.testing.shared.TestExecutionContext;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.plugin.testing.ide.TestServiceClient;
import org.eclipse.che.plugin.testing.ide.handler.TestingHandler;
import org.eclipse.che.plugin.testing.ide.model.GeneralTestingEventsProcessor;
import org.eclipse.che.plugin.testing.ide.view.TestResultPresenter;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

/**
 * @author Mirage Abeysekara
 * @author David Festal
 */
public class RunTestActionDelegate {
    private final Source source;

    public interface Source {
        NotificationManager getNotificationManager();

        AppContext getAppContext();

        TestServiceClient getService();

        TestResultPresenter getPresenter();
        
        String getTestingFramework();

        TestingHandler getTestingHandler();
    }

    public RunTestActionDelegate(Source source) {
        this.source = source;
    }

    public void doRunTests(ActionEvent e, TestExecutionContext context) {
        final StatusNotification notification = new StatusNotification("Running Tests...", PROGRESS, FLOAT_MODE);
        source.getNotificationManager().notify(notification);
        final Project project = source.getAppContext().getRootProject();
        context.setProjectPath(project.getPath());
        context.setFrameworkName(source.getTestingFramework());

        GeneralTestingEventsProcessor eventsProcessor = new GeneralTestingEventsProcessor(source.getTestingFramework(), source.getPresenter().getRootState());
        source.getTestingHandler().setProcessor(eventsProcessor);
        eventsProcessor.addListener(source.getPresenter().getEventListener());

        JsonRpcPromise<Boolean> testResultPromise = source.getService().runTests(context);
        testResultPromise.onSuccess(result -> {
                notification.setStatus(SUCCESS);
            if (result) {
                notification.setTitle("Test runner executed successfully.");
                source.getPresenter().handleResponse();
            } else {
                notification.setTitle("Test runner failed to execute.");
//                    notification.setContent(result.getFailureCount() + " test(s) failed.\n");
            }

        }).onFailure(exception -> {
            final String errorMessage = (exception.getMessage() != null) ? exception.getMessage()
                    : "Failed to run test cases";
            notification.setContent(errorMessage);
            notification.setStatus(FAIL);
        });
    }
}
