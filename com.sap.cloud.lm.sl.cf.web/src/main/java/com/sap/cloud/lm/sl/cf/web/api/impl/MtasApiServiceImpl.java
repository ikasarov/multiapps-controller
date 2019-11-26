package com.sap.cloud.lm.sl.cf.web.api.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.cloudfoundry.client.lib.CloudControllerClient;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.springframework.http.ResponseEntity;

import com.sap.cloud.lm.sl.cf.core.cf.CloudControllerClientProvider;
import com.sap.cloud.lm.sl.cf.core.cf.detect.DeployedComponentsDetector;
import com.sap.cloud.lm.sl.cf.core.model.DeployedComponents;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMta;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaMetadata;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaModule;
import com.sap.cloud.lm.sl.cf.core.util.UserInfo;
import com.sap.cloud.lm.sl.cf.web.api.MtasApiService;
import com.sap.cloud.lm.sl.cf.web.api.model.Metadata;
import com.sap.cloud.lm.sl.cf.web.api.model.Module;
import com.sap.cloud.lm.sl.cf.web.api.model.Mta;
import com.sap.cloud.lm.sl.cf.web.message.Messages;
import com.sap.cloud.lm.sl.cf.web.util.SecurityContextUtil;
import com.sap.cloud.lm.sl.common.NotFoundException;

import com.sap.cloud.lm.sl.cf.web.api.model.ImmutableMetadata;
import com.sap.cloud.lm.sl.cf.web.api.model.ImmutableModule;
import com.sap.cloud.lm.sl.cf.web.api.model.ImmutableMta;

@Named
public class MtasApiServiceImpl implements MtasApiService {

    @Inject
    private CloudControllerClientProvider clientProvider;

    @Override
    public ResponseEntity<List<Mta>> getMtas(String spaceGuid) {
        DeployedComponents deployedComponents = detectDeployedComponents(spaceGuid);
        return ResponseEntity.ok()
                             .body(getMtas(deployedComponents.getMtas()));
    }

    @Override
    public ResponseEntity<Mta> getMta(String spaceGuid, String mtaId) {
        DeployedMta mta = detectDeployedComponents(spaceGuid).findDeployedMta(null, mtaId);
        if (mta == null) {
            throw new NotFoundException(Messages.MTA_NOT_FOUND, mtaId);
        }
        return ResponseEntity.ok()
                             .body(getMta(mta));
    }

    @Override
    public ResponseEntity<List<Mta>> getMtas(String spaceGuid, String namespace, String name) {

        if (name == null && namespace == null) {
            return getMtas(spaceGuid);
        } else if (namespace == null) {
            return getMtasByName(spaceGuid, name);
        } else if (name == null) {
            return getMtasByNamespace(spaceGuid, namespace);
        }

        DeployedComponents deployedComponents = detectDeployedComponents(spaceGuid);
        DeployedMta mta = deployedComponents.findDeployedMta(namespace, name);

        if (mta == null) {
            throw new NotFoundException(Messages.SPECIFIC_MTA_NOT_FOUND, name, namespace);
        }
        return ResponseEntity.ok()
                             .body(getMtas(Arrays.asList(mta)));
    }

    public ResponseEntity<List<Mta>> getMtasByNamespace(String spaceGuid, String namespace) {
        DeployedComponents deployedComponents = detectDeployedComponents(spaceGuid);
        List<DeployedMta> mtas = deployedComponents.findDeployedMtasByNamespace(namespace);

        if (mtas == null || mtas.size() == 0) {
            throw new NotFoundException(Messages.MTAS_NOT_FOUND_BY_NAMESPACE, namespace);
        }

        return ResponseEntity.ok()
                             .body(getMtas(mtas));
    }

    public ResponseEntity<List<Mta>> getMtasByName(String spaceGuid, String name) {
        DeployedComponents deployedComponents = detectDeployedComponents(spaceGuid);
        List<DeployedMta> mtas = deployedComponents.findDeployedMtasByName(name);

        if (mtas == null || mtas.size() == 0) {
            throw new NotFoundException(Messages.MTAS_NOT_FOUND_BY_NAME, name);
        }

        return ResponseEntity.ok()
                             .body(getMtas(mtas));
    }

    private DeployedComponents detectDeployedComponents(String spaceGuid) {
        List<CloudApplication> applications = getCloudFoundryClient(spaceGuid).getApplications(false);
        return new DeployedComponentsDetector().detectAllDeployedComponents(applications);
    }

    private CloudControllerClient getCloudFoundryClient(String spaceGuid) {
        UserInfo userInfo = SecurityContextUtil.getUserInfo();
        return clientProvider.getControllerClient(userInfo.getName(), spaceGuid);
    }

    private List<Mta> getMtas(List<DeployedMta> mtas) {
        return mtas.stream()
                   .map(this::getMta)
                   .collect(Collectors.toList());
    }

    private Mta getMta(DeployedMta mta) {
        return ImmutableMta.builder()
                           .metadata(getMetadata(mta.getMetadata()))
                           .modules(getModules(mta.getModules()))
                           .services(mta.getServices())
                           .build();
    }

    private List<Module> getModules(List<DeployedMtaModule> modules) {
        return modules.stream()
                      .map(this::getModule)
                      .collect(Collectors.toList());
    }

    private Module getModule(DeployedMtaModule module) {
        return ImmutableModule.builder()
                              .appName(module.getAppName())
                              .moduleName(module.getModuleName())
                              .providedDendencyNames(module.getProvidedDependencyNames())
                              .uris(module.getUris())
                              .services(module.getServices())
                              .build();
    }

    private Metadata getMetadata(DeployedMtaMetadata metadata) {
        return ImmutableMetadata.builder()
                                .id(metadata.getId())
                                .version(metadata.getVersion()
                                                 .toString())
                                .namespace(metadata.getNamespace())
                                .build();
    }

}
