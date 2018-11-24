package com.sap.cloud.lm.sl.cf.core.helpers.v1;

import java.util.Map;
import java.util.TreeMap;

import com.sap.cloud.lm.sl.cf.core.model.Parameter;
import com.sap.cloud.lm.sl.mta.model.PropertiesContainer;

public class PropertiesAccessor {

    public Map<String, Object> getProperties(PropertiesContainer propertiesContainer, Map<String, Parameter> supportedParameters) {
        return getOnlyProperties(propertiesContainer.getProperties(), supportedParameters);
    }

    public Map<String, Object> getParameters(PropertiesContainer propertiesContainer, Map<String, Parameter> supportedParameters) {
        return getOnlyParameters(propertiesContainer.getProperties(), supportedParameters);
    }

    public Map<String, Object> getProperties(PropertiesContainer propertiesContainer) {
        return new TreeMap<>(propertiesContainer.getProperties());
    }

    public Map<String, Object> getParameters(PropertiesContainer propertiesContainer) {
        return new TreeMap<>(propertiesContainer.getProperties());
    }

    private Map<String, Object> getOnlyProperties(Map<String, Object> properties, Map<String, Parameter> supportedParameters) {
        Map<String, Object> result = new TreeMap<>(properties);
        result.keySet()
            .removeAll(supportedParameters.keySet());
        return result;
    }

    private Map<String, Object> getOnlyParameters(Map<String, Object> properties, Map<String, Parameter> supportedParameters) {
        Map<String, Object> result = new TreeMap<>(properties);
        result.keySet()
            .retainAll(supportedParameters.keySet());
        return result;
    }

    public void setProperties(PropertiesContainer propertiesContainer, Map<String, Object> properties) {
        propertiesContainer.setProperties(properties);
    }

    public void setParameters(PropertiesContainer propertiesContainer, Map<String, Object> properties) {
        propertiesContainer.setProperties(properties);
    }

}
