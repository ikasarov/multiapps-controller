package com.sap.cloud.lm.sl.cf.core.cf.v3;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sap.cloud.lm.sl.cf.client.lib.domain.CloudApplicationExtended;
import com.sap.cloud.lm.sl.cf.client.lib.domain.CloudServiceExtended;
import com.sap.cloud.lm.sl.cf.core.cf.HandlerFactory;
import com.sap.cloud.lm.sl.cf.core.cf.v1.CloudModelConfiguration;
import com.sap.cloud.lm.sl.cf.core.cf.v2.ApplicationsCloudModelBuilder;
import com.sap.cloud.lm.sl.cf.core.helpers.XsPlaceholderResolver;
import com.sap.cloud.lm.sl.cf.core.helpers.v3.DeployTargetFactory;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMta;
import com.sap.cloud.lm.sl.cf.core.util.UserMessageLogger;
import com.sap.cloud.lm.sl.common.util.Callable;
import com.sap.cloud.lm.sl.common.util.TestUtil;
import com.sap.cloud.lm.sl.common.util.TestUtil.Expectation;
import com.sap.cloud.lm.sl.mta.handlers.v1.DescriptorHandler;
import com.sap.cloud.lm.sl.mta.handlers.v2.DescriptorMerger;
import com.sap.cloud.lm.sl.mta.handlers.v3.ConfigurationParser;
import com.sap.cloud.lm.sl.mta.handlers.v3.DescriptorParser;
import com.sap.cloud.lm.sl.mta.mergers.v2.PlatformMerger;
import com.sap.cloud.lm.sl.mta.mergers.v2.TargetMerger;
import com.sap.cloud.lm.sl.mta.model.SystemParameters;
import com.sap.cloud.lm.sl.mta.model.v1.DeploymentDescriptor;
import com.sap.cloud.lm.sl.mta.model.v1.Platform;
import com.sap.cloud.lm.sl.mta.model.v1.Target;
import com.sap.cloud.lm.sl.mta.resolvers.ResolverBuilder;
import com.sap.cloud.lm.sl.mta.resolvers.v2.DescriptorReferenceResolver;

public class CloudModelBuilderTest extends com.sap.cloud.lm.sl.cf.core.cf.v2.CloudModelBuilderTest {

    @Mock
    private UserMessageLogger userMessageLogger;

    public CloudModelBuilderTest(String deploymentDescriptorLocation, String extensionDescriptorLocation, String platformsLocation,
        String targetsLocation, String deployedMtaLocation, boolean useNamespaces, boolean useNamespacesForServices,
        String[] mtaArchiveModules, String[] mtaModules, String[] deployedApps, Expectation[] expectations) {
        super(deploymentDescriptorLocation, extensionDescriptorLocation, platformsLocation, targetsLocation, deployedMtaLocation,
            useNamespaces, useNamespacesForServices, mtaArchiveModules, mtaModules, deployedApps, expectations);
        MockitoAnnotations.initMocks(this);
    }

    @Parameters
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
// @formatter:off
            // (00) Test missing resource type definition:
            {
                "mtad-missing-resource-type-definition.yaml", "config-01.mtaext", "/mta/platform-types-v2.json", "/mta/targets-v2.json", null,
                false, false,
                new String[] { "foo" }, // mtaArchiveModules
                new String[] { "foo" }, // mtaModules
                new String[] {}, // deployedApps
                new Expectation[] {
                    new Expectation("[]"),
                    new Expectation(Expectation.Type.RESOURCE, "apps-01.json"), }
            },
// @formatter:on
        });
    }

    @Override
    protected DescriptorParser getDescriptorParser() {
        return new DescriptorParser();
    }

    @Override
    protected ConfigurationParser getConfigurationParser() {
        return new ConfigurationParser();
    }

    @Override
    protected com.sap.cloud.lm.sl.mta.handlers.v3.DescriptorHandler getDescriptorHandler() {
        return new com.sap.cloud.lm.sl.mta.handlers.v3.DescriptorHandler();
    }

    @Override
    protected DeployTargetFactory getDeployTargetFactory() {
        return new DeployTargetFactory();
    }

    @Override
    protected DescriptorMerger getDescriptorMerger() {
        return new DescriptorMerger();
    }

    @Override
    protected ServicesCloudModelBuilder getServicesCloudModelBuilder(DeploymentDescriptor deploymentDescriptor,
        CloudModelConfiguration configuration) {
        return new ServicesCloudModelBuilder(deploymentDescriptor, new HandlerFactory(2).getPropertiesAccessor(), configuration,
            userMessageLogger);
    }

    @Override
    protected ApplicationsCloudModelBuilder getApplicationsCloudModelBuilder(DeploymentDescriptor deploymentDescriptor,
        CloudModelConfiguration configuration, DeployedMta deployedMta, SystemParameters systemParameters,
        XsPlaceholderResolver xsPlaceholderResolver) {
        deploymentDescriptor = new DescriptorReferenceResolver((com.sap.cloud.lm.sl.mta.model.v3.DeploymentDescriptor) deploymentDescriptor,
            new ResolverBuilder(), new ResolverBuilder()).resolve();
        return new com.sap.cloud.lm.sl.cf.core.cf.v2.ApplicationsCloudModelBuilder(
            (com.sap.cloud.lm.sl.mta.model.v3.DeploymentDescriptor) deploymentDescriptor, configuration, deployedMta, systemParameters,
            xsPlaceholderResolver, DEPLOY_ID);
    }

    @Override
    protected TargetMerger getTargetMerger(Target target, DescriptorHandler handler) {
        return new com.sap.cloud.lm.sl.mta.mergers.v2.TargetMerger((com.sap.cloud.lm.sl.mta.model.v3.Target) target,
            (com.sap.cloud.lm.sl.mta.handlers.v3.DescriptorHandler) handler);
    }

    @Override
    protected PlatformMerger getPlatformMerger(Platform platform, DescriptorHandler handler) {
        return new com.sap.cloud.lm.sl.mta.mergers.v2.PlatformMerger((com.sap.cloud.lm.sl.mta.model.v3.Platform) platform,
            (com.sap.cloud.lm.sl.mta.handlers.v3.DescriptorHandler) handler);
    }

    @Test
    public void testWarnMessage() {
        servicesBuilder.build();
        Mockito.verify(userMessageLogger)
            .warn(Mockito.anyString(), Mockito.any());
    }

    @Test
    public void testGetApplications() {
        TestUtil.test(new Callable<List<CloudApplicationExtended>>() {
            @Override
            public List<CloudApplicationExtended> call() throws Exception {
                return appsBuilder.build(mtaArchiveModules, mtaModules, deployedApps);
            }
        }, expectations[1], getClass(), new TestUtil.JsonSerializationOptions(false, true));
    }

    @Test
    public void testGetServices() {
        TestUtil.test(new Callable<List<CloudServiceExtended>>() {
            @Override
            public List<CloudServiceExtended> call() throws Exception {
                return servicesBuilder.build();
            }
        }, expectations[0], getClass(), new TestUtil.JsonSerializationOptions(false, true));
    }
}
