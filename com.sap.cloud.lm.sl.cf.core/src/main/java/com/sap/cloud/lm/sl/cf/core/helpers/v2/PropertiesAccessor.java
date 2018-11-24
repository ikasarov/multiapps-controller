package com.sap.cloud.lm.sl.cf.core.helpers.v2;

import java.util.Map;
import java.util.TreeMap;

import com.sap.cloud.lm.sl.cf.core.model.Parameter;
import com.sap.cloud.lm.sl.mta.model.ParametersContainer;
import com.sap.cloud.lm.sl.mta.model.PropertiesContainer;

public class PropertiesAccessor extends com.sap.cloud.lm.sl.cf.core.helpers.v1.PropertiesAccessor {

    @Override
    public Map<String, Object> getParameters(PropertiesContainer propertiesContainer, Map<String, Parameter> supportedParameters) {
        return getParameters((ParametersContainer) propertiesContainer);
    }

    @Override
    public Map<String, Object> getProperties(PropertiesContainer propertiesContainer, Map<String, Parameter> supportedParameters) {
        return new TreeMap<>(propertiesContainer.getProperties());
    }

    public Map<String, Object> getParameters(ParametersContainer parametersContainer) {
        return new TreeMap<>(parametersContainer.getParameters());
    }

    @Override
    public Map<String, Object> getParameters(PropertiesContainer propertiesContainer) {
        return getParameters((ParametersContainer) propertiesContainer);
    }

    @Override
    public void setParameters(PropertiesContainer propertiesContainer, Map<String, Object> parameters) {
        setParameters((ParametersContainer) propertiesContainer, parameters);
    }

    public void setParameters(ParametersContainer parametersContainer, Map<String, Object> parameters) {
        parametersContainer.setParameters(parameters);
    }

}
