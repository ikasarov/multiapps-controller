package com.sap.cloud.lm.sl.cf.core.validators.parameters;

import java.util.Map;

import com.sap.cloud.lm.sl.cf.core.model.Parameter;
import com.sap.cloud.lm.sl.common.util.CommonUtil;
import com.sap.cloud.lm.sl.mta.model.v1.Module;

public class RestartOnEnvChangeValidator implements ParameterValidator {

    @Override
    public boolean isValid(Object restartParameters) {
        if (!(restartParameters instanceof Map)) {
            return false;
        }
        Map<String, Object> parameters = CommonUtil.cast(restartParameters);
        if (parameters.containsKey(Parameter.VCAP_APPLICATION_ENV.getName())
            && !isValidBooleanParameter(parameters.get(Parameter.VCAP_APPLICATION_ENV.getName()))) {
            return false;
        }
        if (parameters.containsKey(Parameter.VCAP_SERVICES_ENV.getName())
            && !isValidBooleanParameter(parameters.get(Parameter.VCAP_SERVICES_ENV.getName()))) {
            return false;
        }
        if (parameters.containsKey(Parameter.USER_PROVIDED_ENV.getName())
            && !isValidBooleanParameter(parameters.get(Parameter.USER_PROVIDED_ENV.getName()))) {
            return false;
        }
        return true;
    }

    private boolean isValidBooleanParameter(Object parameter) {
        return parameter instanceof Boolean;
    }

    @Override
    public Class<?> getContainerType() {
        return Module.class;
    }

    @Override
    public Parameter getParameter() {
        return Parameter.RESTART_ON_ENV_CHANGE;
    }

}
