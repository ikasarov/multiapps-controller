package com.sap.cloud.lm.sl.cf.core.helpers;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.collections4.CollectionUtils;

import com.sap.cloud.lm.sl.cf.core.cf.HandlerFactory;
import com.sap.cloud.lm.sl.cf.core.cf.PlatformType;
import com.sap.cloud.lm.sl.cf.core.helpers.v1.PropertiesAccessor;
import com.sap.cloud.lm.sl.cf.core.message.Messages;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMta;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaModule;
import com.sap.cloud.lm.sl.cf.core.model.Parameter;
import com.sap.cloud.lm.sl.cf.core.model.SupportedParameters;
import com.sap.cloud.lm.sl.cf.core.util.NameUtil;
import com.sap.cloud.lm.sl.cf.core.util.UriUtil;
import com.sap.cloud.lm.sl.cf.core.validators.parameters.HostValidator;
import com.sap.cloud.lm.sl.common.ContentException;
import com.sap.cloud.lm.sl.common.util.CommonUtil;
import com.sap.cloud.lm.sl.mta.model.SystemParameters;
import com.sap.cloud.lm.sl.mta.model.v1.DeploymentDescriptor;
import com.sap.cloud.lm.sl.mta.model.v1.Module;
import com.sap.cloud.lm.sl.mta.model.v1.Resource;

public class SystemParametersBuilder {

    public static final int GENERATED_CREDENTIALS_LENGTH = 16;
    public static final String IDLE_HOST_SUFFIX = "-idle";
    private static final String ROUTE_PATH_PLACEHOLDER = "${route-path}";
    private static final String DEFAULT_URI_HOST_PLACEHOLDER = "${host}.${domain}";
    private static final String DEFAULT_IDLE_URI_HOST_PLACEHOLDER = "${idle-host}.${idle-domain}";
    private static final String DEFAULT_PORT_URI = "${domain}:${port}";
    private static final String DEFAULT_IDLE_PORT_URI = "${idle-domain}:${idle-port}";
    private static final String DEFAULT_URL_PLACEHOLDER = "${protocol}://${default-uri}";
    private static final String DEFAULT_IDLE_URL_PLACEHOLDER = "${protocol}://${default-idle-uri}";

    private static final HostValidator HOST_VALIDATOR = new HostValidator();

    private final CredentialsGenerator credentialsGenerator;
    private final String targetName;
    private final String organization;
    private final String space;
    private final String user;
    private final String defaultDomain;
    private final PlatformType xsType;
    private final URL targetUrl;
    private final String authorizationEndpoint;
    private final String deployServiceUrl;
    private final int routerPort;
    private final boolean portBasedRouting;
    private final PortAllocator portAllocator;
    private final boolean useNamespaces;
    private final boolean useNamespacesForServices;
    private final DeployedMta deployedMta;
    private final boolean reserveTemporaryRoutes;
    private final boolean areXsPlaceholdersSupported;
    private final PropertiesAccessor propertiesAccessor;
    private final Supplier<String> timestampSupplier;

    public SystemParametersBuilder(String platformName, String organization, String space, String user, String defaultDomain,
        PlatformType xsType, URL targetUrl, String authorizationEndpoint, String deployServiceUrl, int routerPort, boolean portBasedRouting,
        boolean reserveTemporaryRoutes, PortAllocator portAllocator, boolean useNamespaces, boolean useNamespacesForServices,
        DeployedMta deployedMta, CredentialsGenerator credentialsGenerator, int majorSchemaVersion, boolean areXsPlaceholdersSupported,
        Supplier<String> timestampSupplier) {
        this.targetName = platformName;
        this.organization = organization;
        this.space = space;
        this.user = user;
        this.defaultDomain = defaultDomain;
        this.xsType = xsType;
        this.targetUrl = targetUrl;
        this.authorizationEndpoint = authorizationEndpoint;
        this.deployServiceUrl = deployServiceUrl;
        this.routerPort = routerPort;
        this.portBasedRouting = portBasedRouting;
        this.portAllocator = portAllocator;
        this.useNamespacesForServices = useNamespacesForServices;
        this.useNamespaces = useNamespaces;
        this.deployedMta = deployedMta;
        this.credentialsGenerator = credentialsGenerator;
        this.reserveTemporaryRoutes = reserveTemporaryRoutes;
        this.areXsPlaceholdersSupported = areXsPlaceholdersSupported;
        this.propertiesAccessor = new HandlerFactory(majorSchemaVersion).getPropertiesAccessor();
        this.timestampSupplier = timestampSupplier;
    }

    public SystemParameters build(DeploymentDescriptor descriptor) {
        Map<String, Map<String, Object>> moduleParameters = new HashMap<>();
        for (Module module : descriptor.getModules1()) {
            moduleParameters.put(module.getName(), getModuleParameters(module, descriptor.getId()));
        }

        Map<String, Map<String, Object>> resourceParameters = new HashMap<>();
        for (Resource resource : descriptor.getResources1()) {
            resourceParameters.put(resource.getName(), getResourceParameters(resource, descriptor.getId()));
        }
        return new SystemParameters(getGeneralParameters(), moduleParameters, resourceParameters,
            SupportedParameters.SINGULAR_PLURAL_MAPPING);
    }

    private Map<String, Object> getGeneralParameters() {
        Map<String, Object> systemParameters = new HashMap<>();

        systemParameters.put(Parameter.DEPLOY_TARGET.getName(), targetName);
        systemParameters.put(Parameter.ORG.getName(), organization);
        systemParameters.put(Parameter.USER.getName(), user);
        systemParameters.put(Parameter.SPACE.getName(), space);
        systemParameters.put(Parameter.DEFAULT_DOMAIN.getName(), getDefaultDomain());
        if (shouldReserveTemporaryRoutes()) {
            systemParameters.put(Parameter.DEFAULT_IDLE_DOMAIN.getName(), getDefaultDomain());
        }
        systemParameters.put(Parameter.XS_TARGET_API_URL.getName(), getTargetUrl());
        systemParameters.put(Parameter.CONTROLLER_URL.getName(), getTargetUrl());
        systemParameters.put(Parameter.XS_TYPE.getName(), xsType.toString());
        systemParameters.put(Parameter.XS_AUTHORIZATION_ENDPOINT.getName(), getAuthorizationEndpoint());
        systemParameters.put(Parameter.AUTHORIZATION_URL.getName(), getAuthorizationEndpoint());
        systemParameters.put(Parameter.DEPLOY_SERVICE_URL.getName(), getDeployServiceUrl());

        return systemParameters;
    }

    private Map<String, Object> getModuleParameters(Module module, String mtaId) {
        Map<String, Object> moduleSystemParameters = new HashMap<>();

        Map<String, Object> moduleParameters = propertiesAccessor.getParameters(module);
        moduleSystemParameters.put(Parameter.DOMAIN.getName(), getDefaultDomain());
        if (shouldReserveTemporaryRoutes()) {
            moduleSystemParameters.put(Parameter.IDLE_DOMAIN.getName(), getDefaultDomain());
        }
        String appName = (String) moduleParameters.getOrDefault(Parameter.APP_NAME.getName(), module.getName());
        moduleSystemParameters.put(Parameter.APP_NAME.getName(), NameUtil.getApplicationName(appName, mtaId, useNamespaces));
        putRoutingParameters(module, moduleParameters, moduleSystemParameters);
        moduleSystemParameters.put(Parameter.COMMAND.getName(), "");
        moduleSystemParameters.put(Parameter.BUILDPACK.getName(), "");
        moduleSystemParameters.put(Parameter.DISK_QUOTA.getName(), -1);
        moduleSystemParameters.put(Parameter.MEMORY.getName(), "256M");
        moduleSystemParameters.put(Parameter.INSTANCES.getName(), 1);
        moduleSystemParameters.put(Parameter.SERVICE.getName(), "");
        moduleSystemParameters.put(Parameter.SERVICE_PLAN.getName(), "");
        moduleSystemParameters.put(Parameter.TIMESTAMP.getName(), getDefaultTimestamp());

        moduleSystemParameters.put(Parameter.GENERATED_USER.getName(), credentialsGenerator.next(GENERATED_CREDENTIALS_LENGTH));
        moduleSystemParameters.put(Parameter.GENERATED_PASSWORD.getName(), credentialsGenerator.next(GENERATED_CREDENTIALS_LENGTH));

        return moduleSystemParameters;
    }

    private String getDefaultTimestamp() {
        return timestampSupplier.get();
    }

    private void putRoutingParameters(Module module, Map<String, Object> moduleParameters, Map<String, Object> moduleSystemParameters) {
        putHostParameters(module, moduleSystemParameters);
        String protocol = getProtocol(moduleParameters);
        if (portBasedRouting || (isTcpOrTcpsProtocol(protocol) && portAllocator != null)) {
            putPortRoutingParameters(module, moduleParameters, moduleSystemParameters);
        } else {
            boolean isStandardPort = UriUtil.isStandardPort(routerPort, targetUrl.getProtocol());
            String defaultUri = isStandardPort ? DEFAULT_URI_HOST_PLACEHOLDER : DEFAULT_URI_HOST_PLACEHOLDER + ":" + getRouterPort();
            if (shouldReserveTemporaryRoutes()) {
                String defaultIdleUri = isStandardPort ? DEFAULT_IDLE_URI_HOST_PLACEHOLDER
                    : DEFAULT_IDLE_URI_HOST_PLACEHOLDER + ":" + getRouterPort();
                moduleSystemParameters.put(Parameter.DEFAULT_IDLE_URI.getName(),
                    appendRoutePathIfPresent(defaultIdleUri, moduleParameters));
                defaultUri = defaultIdleUri;
            }
            moduleSystemParameters.put(Parameter.DEFAULT_URI.getName(), appendRoutePathIfPresent(defaultUri, moduleParameters));
        }

        String defaultUrl = DEFAULT_URL_PLACEHOLDER;
        if (shouldReserveTemporaryRoutes()) {
            String defaultIdleUrl = DEFAULT_IDLE_URL_PLACEHOLDER;
            moduleSystemParameters.put(Parameter.DEFAULT_IDLE_URL.getName(), defaultIdleUrl);
            defaultUrl = defaultIdleUrl;
        }
        moduleSystemParameters.put(Parameter.PROTOCOL.getName(), protocol);
        moduleSystemParameters.put(Parameter.DEFAULT_URL.getName(), defaultUrl);
    }

    private boolean isTcpOrTcpsProtocol(String protocol) {
        return (UriUtil.TCP_PROTOCOL.equals(protocol) || UriUtil.TCPS_PROTOCOL.equals(protocol));
    }

    private void putHostParameters(Module module, Map<String, Object> moduleSystemParameters) {
        String defaultHost = getDefaultHost(module.getName());
        if (shouldReserveTemporaryRoutes()) {
            String idleHost = getDefaultHost(module.getName() + IDLE_HOST_SUFFIX);
            moduleSystemParameters.put(Parameter.DEFAULT_IDLE_HOST.getName(), idleHost);
            moduleSystemParameters.put(Parameter.IDLE_HOST.getName(), idleHost);
            defaultHost = idleHost;
        }
        moduleSystemParameters.put(Parameter.DEFAULT_HOST.getName(), defaultHost);
        moduleSystemParameters.put(Parameter.HOST.getName(), defaultHost);
    }

    private void putPortRoutingParameters(Module module, Map<String, Object> moduleParameters, Map<String, Object> moduleSystemParameters) {

        int defaultPort = getDefaultPort(module.getName(), moduleParameters);
        if (shouldReserveTemporaryRoutes()) {
            int idlePort = allocatePort(moduleParameters);
            moduleSystemParameters.put(Parameter.DEFAULT_IDLE_PORT.getName(), idlePort);
            moduleSystemParameters.put(Parameter.IDLE_PORT.getName(), idlePort);
            moduleSystemParameters.put(Parameter.DEFAULT_IDLE_URI.getName(),
                appendRoutePathIfPresent(DEFAULT_IDLE_PORT_URI, moduleParameters));
            defaultPort = idlePort;
        }
        moduleSystemParameters.put(Parameter.DEFAULT_PORT.getName(), defaultPort);
        moduleSystemParameters.put(Parameter.PORT.getName(), defaultPort);
        moduleSystemParameters.put(Parameter.DEFAULT_URI.getName(), appendRoutePathIfPresent(DEFAULT_PORT_URI, moduleParameters));

    }

    private Object appendRoutePathIfPresent(String uri, Map<String, Object> moduleParameters) {
        if (moduleParameters.containsKey(Parameter.ROUTE_PATH.getName())) {
            return uri + ROUTE_PATH_PLACEHOLDER;
        }
        return uri;
    }

    private Map<String, Object> getResourceParameters(Resource resource, String mtaId) {
        Map<String, Object> resourceSystemParameters = new HashMap<>();

        String serviceName = NameUtil.getServiceName(resource.getName(), mtaId, useNamespaces, useNamespacesForServices);
        resourceSystemParameters.put(Parameter.SERVICE_NAME.getName(), serviceName);
        resourceSystemParameters.put(Parameter.SERVICE.getName(), "");
        resourceSystemParameters.put(Parameter.SERVICE_PLAN.getName(), "");
        resourceSystemParameters.put(Parameter.DEFAULT_CONTAINER_NAME.getName(),
            NameUtil.createValidContainerName(organization, space, resource.getName()));
        resourceSystemParameters.put(Parameter.DEFAULT_XS_APP_NAME.getName(), NameUtil.createValidXsAppName(resource.getName()));

        resourceSystemParameters.put(Parameter.GENERATED_USER.getName(), credentialsGenerator.next(GENERATED_CREDENTIALS_LENGTH));
        resourceSystemParameters.put(Parameter.GENERATED_PASSWORD.getName(), credentialsGenerator.next(GENERATED_CREDENTIALS_LENGTH));

        return resourceSystemParameters;
    }

    private Integer getDefaultPort(String moduleName, Map<String, Object> moduleParameters) {
        DeployedMtaModule deployedModule = getDeployedModule(moduleName);

        if (deployedModule != null && !CollectionUtils.isEmpty(deployedModule.getUris())) {
            Integer usedPort = UriUtil.getPort(deployedModule.getUris()
                .get(0));
            if (usedPort != null) {
                return usedPort;
            }
        }

        return allocatePort(moduleParameters);
    }

    private int allocatePort(Map<String, Object> moduleParameters) {
        boolean isTcpRoute = getBooleanParameter(moduleParameters, Parameter.TCP.getName());
        boolean isTcpsRoute = getBooleanParameter(moduleParameters, Parameter.TCPS.getName());
        if (isTcpRoute && isTcpsRoute) {
            throw new ContentException(Messages.INVALID_TCP_ROUTE);
        }

        if (isTcpRoute || isTcpsRoute) {
            return portAllocator.allocateTcpPort(isTcpsRoute);
        } else {
            return portAllocator.allocatePort();
        }
    }

    private boolean getBooleanParameter(Map<String, Object> moduleParameters, String parameterName) {
        return CommonUtil.cast(moduleParameters.getOrDefault(parameterName, false));
    }

    private DeployedMtaModule getDeployedModule(String moduleName) {
        return deployedMta == null ? null : deployedMta.findDeployedModule(moduleName);
    }

    private String getDefaultHost(String moduleName) {
        String host = (targetName + " " + moduleName).replaceAll("\\s", "-")
            .toLowerCase();
        if (!HOST_VALIDATOR.isValid(host)) {
            return HOST_VALIDATOR.attemptToCorrect(host);
        }
        return host;
    }

    private boolean shouldReserveTemporaryRoutes() {
        return reserveTemporaryRoutes;
    }

    private String getDeployServiceUrl() {
        if (shouldUseXsPlaceholders()) {
            return SupportedParameters.XSA_DEPLOY_SERVICE_URL_PLACEHOLDER;
        }
        return deployServiceUrl;
    }

    private String getDefaultDomain() {
        if (shouldUseXsPlaceholders()) {
            return SupportedParameters.XSA_DEFAULT_DOMAIN_PLACEHOLDER;
        }
        return defaultDomain;
    }

    private Object getAuthorizationEndpoint() {
        if (shouldUseXsPlaceholders()) {
            return SupportedParameters.XSA_AUTHORIZATION_ENDPOINT_PLACEHOLDER;
        }
        return authorizationEndpoint;
    }

    private String getRouterPort() {
        if (shouldUseXsPlaceholders()) {
            return SupportedParameters.XSA_ROUTER_PORT_PLACEHOLDER;
        }
        return Integer.toString(routerPort);
    }

    private String getTargetUrl() {
        if (shouldUseXsPlaceholders()) {
            return SupportedParameters.XSA_CONTROLLER_ENDPOINT_PLACEHOLDER;
        }
        return targetUrl.toString();
    }

    private String getProtocol(Map<String, Object> moduleParameters) {
        boolean isTcpRoute = getBooleanParameter(moduleParameters, Parameter.TCP.getName());
        boolean isTcpsRoute = getBooleanParameter(moduleParameters, Parameter.TCPS.getName());
        if (isTcpRoute) {
            return UriUtil.TCP_PROTOCOL;
        }
        if (isTcpsRoute) {
            return UriUtil.TCPS_PROTOCOL;
        }
        if (shouldUseXsPlaceholders()) {
            return SupportedParameters.XSA_PROTOCOL_PLACEHOLDER;
        }
        return targetUrl.getProtocol();
    }

    private boolean shouldUseXsPlaceholders() {
        return xsType.equals(PlatformType.XS2) && areXsPlaceholdersSupported;
    }
}
