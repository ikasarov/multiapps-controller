package com.sap.cloud.lm.sl.cf.core.helpers.v1;

import java.util.HashMap;
import java.util.Map;

import com.sap.cloud.lm.sl.cf.core.model.Parameter;
import com.sap.cloud.lm.sl.mta.model.v1.Target;

public class DeployTargetFactory {

    public Target create(String org, String space, String targetType) {
        Target.Builder builder = new Target.Builder();
        builder.setType(targetType);
        builder.setProperties(createImplicitTargetProperties(org, space));
        return builder.build();
    }

    protected Map<String, Object> createImplicitTargetProperties(String org, String space) {
        Map<String, Object> properties = new HashMap<>();

        properties.put(Parameter.SPACE.getName(), space);
        properties.put(Parameter.ORG.getName(), org);
        return properties;
    }
}
