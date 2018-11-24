package com.sap.cloud.lm.sl.cf.core.parser;

import static com.sap.cloud.lm.sl.mta.util.PropertiesUtil.getPropertyValue;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.sap.cloud.lm.sl.cf.client.lib.domain.RestartParameters;
import com.sap.cloud.lm.sl.cf.core.model.Parameter;

public class RestartParametersParser implements ParametersParser<RestartParameters> {

    @Override
    public RestartParameters parse(List<Map<String, Object>> parametersList) {
        Map<String, Boolean> restartParameters = getRestartParametersFromDescriptor(parametersList);
        boolean shouldRestartOnVcapAppChange = restartParameters.getOrDefault(Parameter.VCAP_APPLICATION_ENV.getName(), true);
        boolean shouldRestartOnVcapServicesChange = restartParameters.getOrDefault(Parameter.VCAP_SERVICES_ENV.getName(), true);
        boolean shouldRestartOnUserProvidedChange = restartParameters.getOrDefault(Parameter.USER_PROVIDED_ENV.getName(), true);
        return new RestartParameters(shouldRestartOnVcapAppChange, shouldRestartOnVcapServicesChange, shouldRestartOnUserProvidedChange);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Boolean> getRestartParametersFromDescriptor(List<Map<String, Object>> parametersList) {
        return (Map<String, Boolean>) getPropertyValue(parametersList, Parameter.RESTART_ON_ENV_CHANGE.getName(), Collections.emptyMap());
    }

}
