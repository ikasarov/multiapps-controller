package com.sap.cloud.lm.sl.cf.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sap.cloud.lm.sl.mta.util.PropertiesUtil;

public class SupportedParameters {

    // XSA placeholders:
    public static final String XSA_CONTROLLER_ENDPOINT_PLACEHOLDER = "{xsa-placeholder-endpoint-controller}";
    public static final String XSA_DEFAULT_DOMAIN_PLACEHOLDER = "{xsa-placeholder-domain-default}";
    public static final String XSA_PROTOCOL_PLACEHOLDER = "{xsa-placeholder-protocol}";
    public static final String XSA_ROUTER_PORT_PLACEHOLDER = "{xsa-placeholder-router-port}";
    public static final String XSA_AUTHORIZATION_ENDPOINT_PLACEHOLDER = "{xsa-placeholder-endpoint-authorization}";
    public static final String XSA_DEPLOY_SERVICE_URL_PLACEHOLDER = "{xsa-placeholder-service-url-deploy-service}";

    public static final Map<String, Parameter> CONFIGURATION_REFERENCE_PARAMETERS = Parameter.nameIndexOf(Parameter.PROVIDER_NID,
        Parameter.PROVIDER_ID, Parameter.TARGET, Parameter.VERSION, Parameter.MTA_ID, Parameter.MTA_VERSION,
        Parameter.MTA_PROVIDES_DEPENDENCY);

    public static final Map<String, Parameter> APP_PROPS = Parameter.nameIndexOf(Parameter.APP_NAME, Parameter.HOST, Parameter.DOMAIN,
        Parameter.PORT, Parameter.HOSTS, Parameter.DOMAINS, Parameter.PORTS, Parameter.COMMAND, Parameter.BUILDPACK,
        Parameter.HEALTH_CHECK_TYPE, Parameter.HEALTH_CHECK_HTTP_ENDPOINT, Parameter.ENABLE_SSH, Parameter.STACK,
        Parameter.HEALTH_CHECK_TIMEOUT, Parameter.IDLE_HOST, Parameter.MEMORY, Parameter.INSTANCES, Parameter.NO_HOSTNAME,
        Parameter.NO_ROUTE, Parameter.IDLE_PORT, Parameter.IDLE_DOMAIN, Parameter.DISK_QUOTA, Parameter.IDLE_PORTS, Parameter.IDLE_DOMAINS,
        Parameter.IDLE_HOSTS, Parameter.TASKS, Parameter.RESTART_ON_ENV_CHANGE, Parameter.VCAP_APPLICATION_ENV, Parameter.VCAP_SERVICES_ENV,
        Parameter.USER_PROVIDED_ENV, Parameter.KEEP_EXISTING_ROUTES);

    public static final Map<String, Parameter> SERVICE_PROPS = Parameter.nameIndexOf(Parameter.SERVICE_NAME, Parameter.SERVICE,
        Parameter.SERVICE_PLAN, Parameter.SERVICE_ALTERNATIVES, Parameter.SERVICE_PROVIDER, Parameter.SERVICE_VERSION,
        Parameter.SERVICE_CONFIG, Parameter.SERVICE_CONFIG_PATH, Parameter.SERVICE_TAGS, Parameter.SERVICE_KEY_NAME);

    public static final Map<String, Parameter> APP_ATTRIBUTES = Parameter.nameIndexOf(Parameter.EXECUTE_APP, Parameter.SUCCESS_MARKER,
        Parameter.FAILURE_MARKER, Parameter.STOP_APP, Parameter.CHECK_DEPLOY_ID, Parameter.REGISTER_SERVICE_URL,
        Parameter.REGISTER_SERVICE_URL_SERVICE_NAME, Parameter.REGISTER_SERVICE_URL_SERVICE_URL, Parameter.CREATE_SERVICE_BROKER,
        Parameter.SERVICE_BROKER_NAME, Parameter.SERVICE_BROKER_USERNAME, Parameter.SERVICE_BROKER_PASSWORD, Parameter.SERVICE_BROKER_URL,
        Parameter.SERVICE_BROKER_SPACE_SCOPED, Parameter.DEPENDENCY_TYPE, Parameter.NO_START, Parameter.UPLOAD_TIMEOUT);

    public static final Map<String, Parameter> SPECIAL_MT_PROPS = Parameter.nameIndexOf(Parameter.DEFAULT_RT);

    public static final Map<String, Parameter> SPECIAL_RT_PROPS = Parameter.nameIndexOf(Parameter.TYPE);

    public static final Map<String, Parameter> ALL_PARAMETERS = Parameter.nameIndexOf(Parameter.values());

    public static final Map<String, String> SINGULAR_PLURAL_MAPPING = Collections.unmodifiableMap(Arrays.asList(Parameter.values())
        .stream()
        .filter(p -> p.getPlural() != null)
        .collect(Collectors.toMap(Parameter::getName, p -> p.getPlural()
            .getName())));

    // public static final Map<String, String> SINGULAR_PLURAL_MAPPING;
    //
    // static {
    // Map<String, String> prototype = new HashMap<>();
    // prototype.put(IDLE_HOST, IDLE_HOSTS);
    // prototype.put(IDLE_DOMAIN, IDLE_DOMAINS);
    // prototype.put(IDLE_PORT, IDLE_PORTS);
    //
    // prototype.put(ROUTE, ROUTES);
    // prototype.put(HOST, HOSTS);
    // prototype.put(DOMAIN, DOMAINS);
    // prototype.put(PORT, PORTS);
    // SINGULAR_PLURAL_MAPPING = Collections.unmodifiableMap(prototype);
    // }

    public enum RoutingParameterSet {
        // @formatter:off
        ACTUAL(Parameter.PORT,Parameter.HOST, Parameter.DOMAIN),
        DEFAULT(Parameter.DEFAULT_PORT, Parameter.DEFAULT_HOST, Parameter.DEFAULT_DOMAIN),
        IDLE(Parameter.IDLE_PORT, Parameter.IDLE_HOST, Parameter.IDLE_DOMAIN),
        DEFAULT_IDLE(Parameter.DEFAULT_IDLE_PORT, Parameter.DEFAULT_IDLE_HOST, Parameter.DEFAULT_IDLE_DOMAIN);
        // @formatter:on
        public final String port;
        public final String domain;
        public final String host;

        RoutingParameterSet(Parameter port, Parameter host, Parameter domain) {
            this.port = port.getName();
            this.host = host.getName();
            this.domain = domain.getName();

        }
    }

    public static <T> List<T> getAll(List<Map<String, Object>> propertiesList, Parameter param) {
        List<T> result = new ArrayList<>();
        if (param == null) {
            return result;
        }
        T value = (T) PropertiesUtil.getPropertyValue(propertiesList, param.getName(), null);
        List<T> values = null;
        if (param.getPlural() != null) {
            values = (List<T>) PropertiesUtil.getPropertyValue(propertiesList, param.getPlural()
                .getName(), null);
        }
        if (value != null) {
            result.add(value);
        }
        if (values != null) {
            result.addAll(values);
        }
        return result;
    }

}
