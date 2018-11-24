package com.sap.cloud.lm.sl.cf.core.parser;

import static com.sap.cloud.lm.sl.mta.util.PropertiesUtil.getAll;
import static com.sap.cloud.lm.sl.mta.util.PropertiesUtil.getPropertyValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.sap.cloud.lm.sl.cf.core.model.Parameter;
import com.sap.cloud.lm.sl.cf.core.model.SupportedParameters;
import com.sap.cloud.lm.sl.cf.core.util.UriUtil;

public class UriParametersParser implements ParametersParser<List<String>> {

    private boolean portBasedRouting;
    private String defaultHost;
    private String defaultDomain;
    private Integer defaultPort;
    private Parameter hostParameter;
    private Parameter domainParameter;
    private Parameter portParameter;
    private Parameter routeParameter;
    private String routePath;
    private boolean includeProtocol;
    private String protocol;

    public UriParametersParser(boolean portBasedRouting, String defaultHost, String defaultDomain, Integer defaultPort, String routePath,
        boolean includeProtocol, String protocol) {
        this(portBasedRouting, defaultHost, defaultDomain, defaultPort, Parameter.HOST, Parameter.DOMAIN, Parameter.PORT, Parameter.ROUTE,
            routePath, includeProtocol, protocol);
    }

    public UriParametersParser(boolean portBasedRouting, String defaultHost, String defaultDomain, Integer defaultPort,
        Parameter hostParameter, Parameter domainParameter, Parameter portParameter, Parameter routeParameter, String routePath,
        boolean includeProtocol, String protocol) {
        this.portBasedRouting = portBasedRouting;
        this.defaultHost = defaultHost;
        this.defaultDomain = defaultDomain;
        this.defaultPort = defaultPort;
        this.hostParameter = hostParameter;
        this.domainParameter = domainParameter;
        this.portParameter = portParameter;
        this.routeParameter = routeParameter;
        this.routePath = routePath;
        this.includeProtocol = includeProtocol;
        this.protocol = protocol;
    }

    @Override
    public List<String> parse(List<Map<String, Object>> parametersList) {
        boolean noRoute = (Boolean) getPropertyValue(parametersList, Parameter.NO_ROUTE.getName(), false);
        if (noRoute) {
            return Collections.emptyList();
        }
        return getUris(parametersList);
    }

    public List<Integer> getApplicationPorts(List<Map<String, Object>> parametersList) {
        List<Integer> ports;
        List<String> routes = getApplicationRoutes(parametersList);

        if (!routes.isEmpty()) {
            ports = getPortsFromRoutes(routes);
        } else {
            ports = SupportedParameters.getAll(parametersList, portParameter);
        }

        if (ports.isEmpty() && defaultPort != null && defaultPort != 0) {
            ports.add(defaultPort);
        }
        return ports;
    }

    private void addPortBasedUris(Set<String> uris, String domain, List<Integer> ports) {
        for (Integer port : ports) {
            uris.add(appendRoutePathIfPresent(domain + ":" + port));
        }
    }

    public List<String> getApplicationDomains(List<Map<String, Object>> parametersList) {
        List<String> domains;
        List<String> routes = getApplicationRoutes(parametersList);
        if (!routes.isEmpty()) {
            domains = getDomainsFromRoutes(routes);
        } else {
            domains = SupportedParameters.getAll(parametersList, domainParameter);
        }

        if (domains.isEmpty() && defaultDomain != null) {
            domains.add(defaultDomain);
        }
        return domains;
    }

    private List<String> getApplicationHosts(List<Map<String, Object>> parametersList) {
        boolean noHostname = (Boolean) getPropertyValue(parametersList, Parameter.NO_HOSTNAME.getName(), false);
        if (noHostname) {
            return Collections.emptyList();
        }
        List<String> hosts = SupportedParameters.getAll(parametersList, hostParameter);
        if (hosts.isEmpty() && defaultHost != null) {
            hosts.add(defaultHost);
        }
        return hosts;
    }

    public List<String> getApplicationRoutes(List<Map<String, Object>> parametersList) {
        return SupportedParameters.getAll(parametersList, routeParameter);
    }

    private List<String> getDomainsFromRoutes(List<String> routes) {
        return routes.stream()
            .map(UriUtil::getDomain)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
    }

    private List<Integer> getPortsFromRoutes(List<String> routes) {
        return routes.stream()
            .map(UriUtil::getPort)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
    }

    private void addHostBasedUris(Set<String> uris, String domain, List<String> hosts) {
        for (String host : hosts) {
            uris.add(appendRoutePathIfPresent(host + "." + domain));
        }
    }

    private List<String> getUris(List<Map<String, Object>> parametersList) {

        List<String> routes = getApplicationRoutes(parametersList);
        if (!routes.isEmpty()) {
            return routes;
        }

        List<String> hosts = getApplicationHosts(parametersList);
        List<String> domains = getApplicationDomains(parametersList);
        List<Integer> ports = getApplicationPorts(parametersList);

        if (domains.isEmpty() && hosts.isEmpty()) {
            return new ArrayList<>();
        }
        if (domains.isEmpty() && !hosts.isEmpty()) {
            // Use hosts as domains.
            domains = hosts;
            hosts = Collections.emptyList();
        }

        return assembleUris(hosts, domains, ports);
    }

    private List<String> assembleUris(List<String> hosts, List<String> domains, List<Integer> ports) {
        Set<String> uris = new LinkedHashSet<>();
        for (String domain : domains) {
            if (shouldUsePortBasedUris(ports, hosts)) {
                addPortBasedUris(uris, domain, ports);
            } else if (!hosts.isEmpty()) {
                addHostBasedUris(uris, domain, hosts);
            } else {
                uris.add(appendRoutePathIfPresent(domain));
            }
        }

        return uris.stream()
            .map(this::addProtocol)
            .collect(Collectors.toList());
    }

    private String addProtocol(String uri) {
        if (includeProtocol) {
            return protocol + UriUtil.DEFAULT_SCHEME_SEPARATOR + uri;
        }
        return uri;
    }

    private String appendRoutePathIfPresent(String uri) {
        if (routePath != null) {
            return uri + routePath;
        }
        return uri;
    }

    private boolean shouldUsePortBasedUris(List<Integer> ports, List<String> hosts) {
        return (portBasedRouting || hosts.isEmpty() || isTcpOrTcps()) && !ports.isEmpty();
    }

    private boolean isTcpOrTcps() {
        return Parameter.TCP.getName()
            .equals(protocol)
            || Parameter.TCPS.getName()
                .equals(protocol);
    }

}
