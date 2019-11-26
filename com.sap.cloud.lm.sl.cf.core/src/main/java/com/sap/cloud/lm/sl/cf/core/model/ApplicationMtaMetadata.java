package com.sap.cloud.lm.sl.cf.core.model;

import java.util.List;

import com.sap.cloud.lm.sl.cf.core.Constants;

/**
 * MTA metadata information associated with an application;
 */
public class ApplicationMtaMetadata {

    private final DeployedMtaMetadata mtaMetadata;
    private final List<String> services;
    private final String moduleName;
    private final List<String> providedDependencyNames;

    public ApplicationMtaMetadata(DeployedMtaMetadata mtaMetadata, List<String> services, String moduleName,
                                  List<String> providedDependencyNames) {
        this.mtaMetadata = mtaMetadata;
        this.services = services;
        this.moduleName = moduleName;
        this.providedDependencyNames = providedDependencyNames;
    }

    public DeployedMtaMetadata getMtaMetadata() {
        return mtaMetadata;
    }

    public List<String> getServices() {
        return services;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getQualifiedModuleName() {
        if (this.mtaMetadata.getNamespace() == null) {
            return getModuleName();
        }

        return this.mtaMetadata.getNamespace() + Constants.NAMESPACE_SEPARATOR + this.moduleName;
    }

    public List<String> getProvidedDependencyNames() {
        return providedDependencyNames;
    }

}
