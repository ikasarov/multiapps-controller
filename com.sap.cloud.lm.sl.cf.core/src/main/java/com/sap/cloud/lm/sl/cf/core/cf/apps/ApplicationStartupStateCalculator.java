package com.sap.cloud.lm.sl.cf.core.cf.apps;

import org.cloudfoundry.client.lib.domain.CloudApplication;

import com.sap.cloud.lm.sl.cf.core.helpers.ApplicationAttributes;
import com.sap.cloud.lm.sl.cf.core.model.Parameter;

public class ApplicationStartupStateCalculator {

    public ApplicationStartupState computeDesiredState(CloudApplication app, boolean shouldNotStartAnyApp) {
        if (hasExecuteAppParameter(app)) {
            return ApplicationStartupState.EXECUTED;
        }

        ApplicationAttributes appAttributes = ApplicationAttributes.fromApplication(app);
        boolean shouldNotStartApp = appAttributes.get(Parameter.NO_START.getName(), Boolean.class, shouldNotStartAnyApp);
        return (shouldNotStartApp) ? ApplicationStartupState.STOPPED : ApplicationStartupState.STARTED;
    }

    public ApplicationStartupState computeCurrentState(CloudApplication app) {
        if (hasExecuteAppParameter(app)) {
            return ApplicationStartupState.EXECUTED;
        }
        if (isStarted(app)) {
            return ApplicationStartupState.STARTED;
        }
        if (isStopped(app)) {
            return ApplicationStartupState.STOPPED;
        }
        return ApplicationStartupState.INCONSISTENT;
    }

    private boolean hasExecuteAppParameter(CloudApplication app) {
        ApplicationAttributes appAttributes = ApplicationAttributes.fromApplication(app);
        return appAttributes.get(Parameter.EXECUTE_APP.getName(), Boolean.class, false);
    }

    private org.cloudfoundry.client.lib.domain.CloudApplication.AppState getRequestedState(CloudApplication app) {
        return app.getState();
    }

    private boolean isStarted(CloudApplication app) {
        return app.getRunningInstances() == app.getInstances() && app.getInstances() != 0
            && getRequestedState(app).equals(org.cloudfoundry.client.lib.domain.CloudApplication.AppState.STARTED);
    }

    private boolean isStopped(CloudApplication app) {
        return app.getRunningInstances() == 0
            && getRequestedState(app).equals(org.cloudfoundry.client.lib.domain.CloudApplication.AppState.STOPPED);
    }

}
