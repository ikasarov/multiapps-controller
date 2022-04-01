package org.cloudfoundry.multiapps.controller.core.model;

import org.cloudfoundry.multiapps.common.Nullable;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableDeployedMtaServiceKey.class)
@JsonDeserialize(builder = ImmutableDeployedMtaServiceKey.Builder.class)
// TODO: delete
public interface DeployedMtaServiceKey {

    @Nullable
    public abstract String getModuleName();

    @Nullable
    public abstract String getName();

}
