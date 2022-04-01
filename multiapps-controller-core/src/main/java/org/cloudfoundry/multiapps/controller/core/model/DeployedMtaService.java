package org.cloudfoundry.multiapps.controller.core.model;

import java.util.List;

import org.cloudfoundry.multiapps.common.Nullable;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sap.cloudfoundry.client.facade.domain.CloudServiceInstance;

@Value.Immutable
@JsonSerialize(as = ImmutableDeployedMtaService.class)
@JsonDeserialize(builder = ImmutableDeployedMtaService.Builder.class)
public abstract class DeployedMtaService extends CloudServiceInstance {

    @Nullable
    public abstract String getResourceName();
    
    @Nullable
    public abstract List<String> getMtaServiceKeys();

}
