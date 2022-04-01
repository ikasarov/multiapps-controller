package org.cloudfoundry.multiapps.controller.core.cf.v2;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.cloudfoundry.client.v3.Metadata;
import org.cloudfoundry.client.v3.Metadata.Builder;
import org.cloudfoundry.multiapps.common.util.JsonUtil;
import org.cloudfoundry.multiapps.common.util.MapUtil;
import org.cloudfoundry.multiapps.controller.core.Constants;
import org.cloudfoundry.multiapps.controller.core.cf.metadata.MtaMetadataAnnotations;
import org.cloudfoundry.multiapps.controller.core.cf.metadata.MtaMetadataLabels;
import org.cloudfoundry.multiapps.controller.core.cf.metadata.util.MtaMetadataUtil;
import org.cloudfoundry.multiapps.mta.model.DeploymentDescriptor;
import org.cloudfoundry.multiapps.mta.model.Resource;

import com.sap.cloudfoundry.client.facade.domain.CloudServiceKey;

public class ServiceMetadataBuilder {

    // TODO: remove overload to check all calls are as should be
    public static Metadata build(DeploymentDescriptor deploymentDescriptor, String namespace, Resource resource) {
        return build(deploymentDescriptor, namespace, resource, null);
    }

    public static Metadata build(DeploymentDescriptor deploymentDescriptor, String namespace, Resource resource,
                                 List<CloudServiceKey> serviceKeys) {
        String hashedMtaId = MtaMetadataUtil.getHashedLabel(deploymentDescriptor.getId());
        String mtaResourceAnnotation = buildMtaResourceAnnotation(resource);
        String mtaServiceKeysAnnotation = buildMtaServiceKeysAnnotation(serviceKeys);
        Builder builder = Metadata.builder()
                                  .label(MtaMetadataLabels.MTA_ID, hashedMtaId)
                                  .annotation(MtaMetadataAnnotations.MTA_ID, deploymentDescriptor.getId())
                                  .annotation(MtaMetadataAnnotations.MTA_VERSION, deploymentDescriptor.getVersion())
                                  .annotation(MtaMetadataAnnotations.MTA_RESOURCE, mtaResourceAnnotation);

        if (StringUtils.isNotEmpty(namespace)) {
            String hashedMtaNamespace = MtaMetadataUtil.getHashedLabel(namespace);
            builder.label(MtaMetadataLabels.MTA_NAMESPACE, hashedMtaNamespace)
                   .annotation(MtaMetadataAnnotations.MTA_NAMESPACE, namespace);
        }

        if (StringUtils.isNotEmpty(mtaServiceKeysAnnotation)) {
            builder.annotation(MtaMetadataAnnotations.MTA_SERVICE_KEYS, mtaServiceKeysAnnotation);
        }

        return builder.build();
    }

    private static String buildMtaResourceAnnotation(Resource resource) {
        Map<String, String> mtaModule = new TreeMap<>();
        MapUtil.addNonNull(mtaModule, Constants.ATTR_NAME, resource.getName());
        return JsonUtil.toJson(mtaModule);
    }

    private static String buildMtaServiceKeysAnnotation(List<CloudServiceKey> serviceKeys) {
        List<String> serviceKeyNames = serviceKeys.stream()
                                                  .map(CloudServiceKey::getName)
                                                  .collect(Collectors.toList());
        return serviceKeys != null ? JsonUtil.toJson(serviceKeyNames) : null;
    }

    private ServiceMetadataBuilder() {
    }

}
