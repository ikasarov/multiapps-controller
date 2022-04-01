package org.cloudfoundry.multiapps.controller.core.cf.v3;

import java.util.List;
import java.util.Map;

import org.cloudfoundry.multiapps.mta.model.DeploymentDescriptor;
import org.cloudfoundry.multiapps.mta.model.Resource;

import com.sap.cloudfoundry.client.facade.domain.CloudServiceKey;

public class ServicesCloudModelBuilder extends org.cloudfoundry.multiapps.controller.core.cf.v2.ServicesCloudModelBuilder {

    public ServicesCloudModelBuilder(DeploymentDescriptor deploymentDescriptor, String namespace,
                                     Map<String, List<CloudServiceKey>> serviceKeysByResources) {
        super(deploymentDescriptor, namespace, serviceKeysByResources);
    }

    @Override
    protected CommonServiceParameters getCommonServiceParameters(Resource resource) {
        return new CommonServiceParametersV3(resource);
    }

    static class CommonServiceParametersV3 extends CommonServiceParameters {

        CommonServiceParametersV3(Resource resource) {
            super(resource);
        }

        @Override
        protected boolean isOptional() {
            return resource.isOptional();
        }
    }

}
