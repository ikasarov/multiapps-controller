package com.sap.cloud.lm.sl.cf.core.parser;

import com.sap.cloud.lm.sl.cf.core.model.Parameter;

public class IdleUriParametersParser extends UriParametersParser {

    public IdleUriParametersParser(boolean portBasedRouting, String defaultHost, String defaultDomain, Integer defaultPort,
        String routePath, boolean includeProtocol, String protocol) {
        super(portBasedRouting, defaultHost, defaultDomain, defaultPort, Parameter.IDLE_HOST, Parameter.IDLE_DOMAIN,
            Parameter.IDLE_PORT, null, routePath, includeProtocol, protocol);
    }

    public IdleUriParametersParser(boolean portBasedRouting, String defaultHost, String defaultDomain, Integer defaultPort,
        Parameter hostParameter, Parameter domainParameter, Parameter portParameter, String routePath, boolean includeProtocol,
        String protocol) {
        super(portBasedRouting, defaultHost, defaultDomain, defaultPort, hostParameter, domainParameter, portParameter, null,
            routePath, includeProtocol, protocol);
    }

}
