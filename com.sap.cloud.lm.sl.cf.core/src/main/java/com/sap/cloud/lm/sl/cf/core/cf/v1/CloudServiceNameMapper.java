package com.sap.cloud.lm.sl.cf.core.cf.v1;

import java.util.Map;

import com.sap.cloud.lm.sl.cf.core.helpers.v1.PropertiesAccessor;
import com.sap.cloud.lm.sl.cf.core.model.Parameter;
import com.sap.cloud.lm.sl.cf.core.util.NameUtil;
import com.sap.cloud.lm.sl.mta.model.v1.DeploymentDescriptor;
import com.sap.cloud.lm.sl.mta.model.v1.Resource;

public class CloudServiceNameMapper {

    private CloudModelConfiguration configuration;
    private PropertiesAccessor propertiesAccessor;
    private DeploymentDescriptor deploymentDescriptor;

    public CloudServiceNameMapper(CloudModelConfiguration configuration, PropertiesAccessor propertiesAccessor,
        DeploymentDescriptor deploymentDescriptor) {
        this.configuration = configuration;
        this.propertiesAccessor = propertiesAccessor;
        this.deploymentDescriptor = deploymentDescriptor;
    }

    public String mapServiceName(Resource resource, ResourceType serviceType) {
        Map<String, Object> parameters = propertiesAccessor.getParameters(resource);
        String overwritingName = (String) parameters.get(Parameter.SERVICE_NAME.getName());

        String shortServiceName = overwritingName != null ? overwritingName : resource.getName();
        if (serviceType.equals(ResourceType.EXISTING_SERVICE)) {
            return shortServiceName;
        }
        return getServiceName(shortServiceName);
    }

    public String getServiceName(String name) {
        return NameUtil.getServiceName(name, deploymentDescriptor.getId(), configuration.shouldUseNamespaces(),
            configuration.shouldUseNamespacesForServices());
    }
}
