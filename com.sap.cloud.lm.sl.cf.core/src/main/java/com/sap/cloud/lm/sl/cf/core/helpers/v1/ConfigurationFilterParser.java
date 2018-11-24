package com.sap.cloud.lm.sl.cf.core.helpers.v1;

import static com.sap.cloud.lm.sl.cf.core.util.ConfigurationEntriesUtil.PROVIDER_NID;
import static com.sap.cloud.lm.sl.mta.util.PropertiesUtil.getOptionalParameter;
import static com.sap.cloud.lm.sl.mta.util.PropertiesUtil.getRequiredParameter;
import static com.sap.cloud.lm.sl.mta.util.PropertiesUtil.mergeProperties;

import java.util.Map;

import com.sap.cloud.lm.sl.cf.core.dao.filters.ConfigurationFilter;
import com.sap.cloud.lm.sl.cf.core.model.CloudTarget;
import com.sap.cloud.lm.sl.cf.core.model.Parameter;
import com.sap.cloud.lm.sl.cf.core.util.ConfigurationEntriesUtil;
import com.sap.cloud.lm.sl.common.util.Pair;
import com.sap.cloud.lm.sl.mta.builders.v1.PropertiesChainBuilder;
import com.sap.cloud.lm.sl.mta.model.v1.Platform;
import com.sap.cloud.lm.sl.mta.model.v1.Resource;
import com.sap.cloud.lm.sl.mta.model.v1.Target;

public class ConfigurationFilterParser {

    private static final String NEW_SYNTAX_FILTER = "configuration";
    private static final String OLD_SYNTAX_FILTER = "mta-provides-dependency";

    protected Platform platform;
    protected Target target;
    protected PropertiesChainBuilder chainBuilder;

    public ConfigurationFilterParser(Platform platform, Target target, PropertiesChainBuilder chainBuilder) {
        this.platform = platform;
        this.target = target;
        this.chainBuilder = chainBuilder;
    }

    public ConfigurationFilter parse(Resource resource) {
        String type = getType(resource);
        if (OLD_SYNTAX_FILTER.equals(type)) {
            return parseOldSyntaxFilter(resource);
        }
        if (NEW_SYNTAX_FILTER.equals(type)) {
            return parseNewSyntaxFilter(resource);
        }
        return null;
    }

    private String getType(Resource resource) {
        Map<String, Object> mergedParameters = mergeProperties(chainBuilder.buildResourceChain(resource.getName()));
        return (String) mergedParameters.get(Parameter.TYPE.getName());
    }

    private ConfigurationFilter parseOldSyntaxFilter(Resource resource) {
        Map<String, Object> parameters = getParameters(resource);
        String mtaId = getRequiredParameter(parameters, Parameter.MTA_ID.getName());
        CloudTarget cloudTarget = getCurrentOrgAndSpace();
        String mtaProvidesDependency = getRequiredParameter(parameters, Parameter.MTA_PROVIDES_DEPENDENCY.getName());
        String mtaVersion = getRequiredParameter(parameters, Parameter.MTA_VERSION.getName());
        String providerId = ConfigurationEntriesUtil.computeProviderId(mtaId, mtaProvidesDependency);
        return new ConfigurationFilter(PROVIDER_NID, providerId, mtaVersion, cloudTarget, null);
    }

    private ConfigurationFilter parseNewSyntaxFilter(Resource resource) {
        Map<String, Object> parameters = getParameters(resource);
        String version = getOptionalParameter(parameters, Parameter.VERSION.getName());
        String namespaceId = getOptionalParameter(parameters, Parameter.PROVIDER_NID.getName());
        String pid = getOptionalParameter(parameters, Parameter.PROVIDER_ID.getName());
        Map<String, Object> filter = getOptionalParameter(parameters, Parameter.FILTER.getName());
        Map<String, Object> target = getOptionalParameter(parameters, Parameter.TARGET.getName());
        boolean hasExplicitTarget = target != null;
        CloudTarget cloudTarget = hasExplicitTarget ? parseSpaceTarget(target) : getCurrentOrgAndSpace();
        return new ConfigurationFilter(namespaceId, pid, version, cloudTarget, filter, hasExplicitTarget);

    }

    private CloudTarget parseSpaceTarget(Map<String, Object> target) {
        String org = getRequiredParameter(target, Parameter.ORG.getName());
        String space = getRequiredParameter(target, Parameter.SPACE.getName());
        return new CloudTarget(org, space);
    }

    protected Map<String, Object> getParameters(Resource resource) {
        return resource.getProperties();
    }

    protected CloudTarget getCurrentOrgAndSpace() {
        Pair<String, String> currentOrgSpace = new OrgAndSpaceHelper(target, platform).getOrgAndSpace();
        return new CloudTarget(currentOrgSpace._1, currentOrgSpace._2);
    }
}
