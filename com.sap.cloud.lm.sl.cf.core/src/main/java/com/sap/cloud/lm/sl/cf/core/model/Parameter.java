package com.sap.cloud.lm.sl.cf.core.model;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum Parameter {
    // @formatter:off
        // General parameters:
    USER("user"),
    DEFAULT_DOMAIN("default-domain"),
    DEPLOY_TARGET("deploy-target"),
    PROTOCOL("protocol"),
    XS_TYPE("xs-type"),
        @Deprecated
    XS_TARGET_API_URL("xs-api-url"),
        @Deprecated
    XS_AUTHORIZATION_ENDPOINT("xs-auth-url"),
    CONTROLLER_URL("controller-url"),
    AUTHORIZATION_URL("authorization-url"),
    DEPLOY_SERVICE_URL("deploy-url"),
    GENERATED_USER("generated-user"),
    GENERATED_PASSWORD("generated-password"),
    DEFAULT_IDLE_DOMAIN("default-idle-domain"),
    TIMESTAMP("timestamp"),
    ENABLE_PARALLEL_DEPLOYMENTS("enable-parallel-deployments"),

        // Module / module type parameters:
    APP_NAME("app-name"),
    @Deprecated
    DOMAIN("domain"),
    @Deprecated
    DOMAINS("domains", DOMAIN),
    @Deprecated
    HOST("host"),
    @Deprecated
    HOSTS("hosts", HOST),
    @Deprecated
    PORT("port"),
    @Deprecated
    PORTS("ports", PORT),
    ROUTE("route"),
    ROUTES("routes", ROUTE),
    DEFAULT_HOST("default-host"),
    DEFAULT_PORT("default-port"),
    KEEP_EXISTING_ROUTES("keep-existing-routes"),
    TCP("tcp"),
    TCPS("tcps"),
    COMMAND("command"),
    BUILDPACK("buildpack"),
    STACK("stack"),
    HEALTH_CHECK_TIMEOUT("health-check-timeout"),
    HEALTH_CHECK_TYPE("health-check-type"),
    HEALTH_CHECK_HTTP_ENDPOINT("health-check-http-endpoint"),
    UPLOAD_TIMEOUT("upload-timeout"),
    DISK_QUOTA("disk-quota"),
    MEMORY("memory"),
    INSTANCES("instances"),
    ENABLE_SSH("enable-ssh"),
    NO_HOSTNAME("no-hostname"),
    NO_ROUTE("no-route"),
    DEFAULT_URI("default-uri"),
    DEFAULT_IDLE_URI("default-idle-uri"),
    DEFAULT_URL("default-url"),
    DEFAULT_IDLE_URL("default-idle-url"),
    ROUTE_PATH("route-path"),
    DEFAULT_IDLE_HOST("default-idle-host"),
    DEFAULT_IDLE_PORT("default-idle-port"),
    IDLE_PORT("idle-port"),
    IDLE_PORTS("idle-ports", IDLE_PORT),
    IDLE_DOMAIN("idle-domain"),
    IDLE_DOMAINS("idle-domains", IDLE_DOMAIN),
    IDLE_HOST("idle-host"),
    IDLE_HOSTS("idle-hosts", IDLE_HOST),
    CREATE_USER_PROVIDED_SERVICE("create-user-provided-service"),
    USER_PROVIDED_SERVICE_NAME("user-provided-service-name"),
    USER_PROVIDED_SERVICE_CONFIG("user-provided-service-config"),
    DEPENDENCY_TYPE("dependency-type"),
    TASKS("tasks"),
    RESTART_ON_ENV_CHANGE("restart-on-env-change"),
    VCAP_APPLICATION_ENV("vcap-application"),
    VCAP_SERVICES_ENV("vcap-services"),
    USER_PROVIDED_ENV("user-provided"),
    EXECUTE_APP("execute-app"),
    SUCCESS_MARKER("success-marker"),
    FAILURE_MARKER("failure-marker"),
    STOP_APP("stop-app"),
    NO_START("no-start"),
    CHECK_DEPLOY_ID("check-deploy-id"),
    REGISTER_SERVICE_URL("register-service-url"),
    REGISTER_SERVICE_URL_SERVICE_NAME("service-name"),
    REGISTER_SERVICE_URL_SERVICE_URL("service-url"),
    CREATE_SERVICE_BROKER("create-service-broker"),
    SERVICE_BROKER_NAME("service-broker-name"),
    SERVICE_BROKER_USERNAME("service-broker-user"),
    SERVICE_BROKER_PASSWORD("service-broker-password"),
    SERVICE_BROKER_URL("service-broker-url"),
    SERVICE_BROKER_SPACE_SCOPED("service-broker-space-scoped"),
    DEFAULT_RT("default-resource-type"),

        // Required dependency parameters:
    SERVICE_BINDING_CONFIG("config"),
    SERVICE_BINDING_CONFIG_PATH("config-path"),
    MANAGED("managed"),
    ENV_VAR_NAME("env-var-name"),

        // Resource / resource type parameters:
    SERVICE_NAME("service-name"),
    SERVICE("service"),
    SERVICE_PLAN("service-plan"),
    SERVICE_ALTERNATIVES("service-alternatives"),
    SERVICE_PROVIDER("service-provider"),
    SERVICE_VERSION("service-version"),
    SERVICE_CONFIG("config"),
    SERVICE_CONFIG_PATH("config-path"),
    SERVICE_TAGS("service-tags"),
    SERVICE_KEYS("service-keys"),
    NAME("name"),
    SERVICE_KEY_CONFIG("config"),
    SERVICE_KEY_NAME("service-key-name"),
    SHARED("shared"),
    DEFAULT_CONTAINER_NAME("default-container-name"),
    DEFAULT_XS_APP_NAME("default-xsappname"),
    TYPE("type"),
    IGNORE_UPDATE_ERRORS("ignore-update-errors"),

        // Configuration reference (new syntax):
    PROVIDER_NID("provider-nid"),
    VERSION("version"),
    PROVIDER_ID("provider-id"),
    TARGET("target"),
    FILTER("filter"),
    VISIBILITY("visibility"),
        // Configuration reference (old syntax):
    MTA_VERSION("mta-version"),
    MTA_ID("mta-id"),
    MTA_MODULE("mta-module"),
    MTA_PROVIDES_DEPENDENCY("mta-provides-dependency"),

        // Platform / platform type parameters:
    ORG("org"),
    SPACE("space");
    // @formatter:on

    static {
        initIsDeprecated();
    }

    private final String name;
    private Parameter singular = null;
    private Parameter plural = null;
    private boolean isDeprecated = false;

    Parameter(String name) {
        this.name = name;
    }

    Parameter(String name, Parameter singular) {
        this.name = name;
        this.singular = singular;
        singular.setPlural(this);
    }

    private void setPlural(Parameter plural) {
        this.plural = plural;
    }

    private static void initIsDeprecated() {
        for (Parameter param : Parameter.values()) {
            try {
                Field field = Parameter.class.getField(param.toString());
                if (field.isAnnotationPresent(Deprecated.class)) {
                    param.isDeprecated = true;
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public static Map<String, Parameter> nameIndexOf(Parameter... params) {
        Map<String, Parameter> prototypeMap = new HashMap<>();

        if (params == null) {
            return new HashMap<>();
        }

        for (Parameter param : params) {
            prototypeMap.put(param.name, param);
        }
        return Collections.unmodifiableMap(prototypeMap);
    }

    public String getName() {
        return name;
    }

    public boolean hasPlural() {
        return plural != null;
    }

    public Parameter getPlural() {
        return plural;
    }

    public String getPluralName() {
        if (!hasPlural()) {
            return null;
        }

        return getPlural().getName();
    }

    public boolean isDeprecated() {
        return isDeprecated;
    }

    public boolean hasSingular() {
        return singular != null;
    }

    public Parameter getSingular() {
        return singular;
    }

    public String getSingularName() {
        if (!hasSingular()) {
            return null;
        }

        return getSingular().getName();
    }
}