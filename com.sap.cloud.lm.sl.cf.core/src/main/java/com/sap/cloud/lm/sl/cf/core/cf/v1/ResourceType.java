package com.sap.cloud.lm.sl.cf.core.cf.v1;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import com.sap.cloud.lm.sl.cf.core.model.Parameter;

public enum ResourceType {
    MANAGED_SERVICE("managed-service", Parameter.SERVICE, Parameter.SERVICE_PLAN), USER_PROVIDED_SERVICE(
        "user-provided-service", Parameter.SERVICE_CONFIG), EXISTING_SERVICE("existing-service"), EXISTING_SERVICE_KEY("existing-service-key");

    private String name;
    private final Map<String, Parameter> requiredParameters;

    private ResourceType(String value, Parameter... requiredParameters) {
        this.name = value;
        this.requiredParameters = Parameter.nameIndexOf(requiredParameters);
    }

    @Override
    public String toString() {
        return name;
    }

    public static ResourceType get(String value) {
        for (ResourceType v : values()) {
            if (v.name.equals(value))
                return v;
        }
        return null;
    }

    public static Set<ResourceType> getServiceTypes() {
        return EnumSet.of(MANAGED_SERVICE, USER_PROVIDED_SERVICE, EXISTING_SERVICE);
    }

    public Set<String> getRequiredParameterNames() {
        return requiredParameters.keySet();
    }
    
}