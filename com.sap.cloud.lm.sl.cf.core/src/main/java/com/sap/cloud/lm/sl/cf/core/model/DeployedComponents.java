package com.sap.cloud.lm.sl.cf.core.model;

import java.util.List;
import java.util.stream.Collectors;

public class DeployedComponents {

    private List<DeployedMta> mtas;
    private List<String> standaloneApps;

    public DeployedComponents() {
    }

    public DeployedComponents(List<DeployedMta> mtas, List<String> standaloneApps) {
        this.mtas = mtas;
        this.standaloneApps = standaloneApps;
    }

    public List<DeployedMta> getMtas() {
        return mtas;
    }

    public void setMtas(List<DeployedMta> mtas) {
        this.mtas = mtas;
    }

    public List<String> getStandaloneApps() {
        return standaloneApps;
    }

    public void setStandaloneApps(List<String> standaloneApps) {
        this.standaloneApps = standaloneApps;
    }

    public List<DeployedMta> findDeployedMtasByName(String name) {
        return getMtas().stream()
                        .filter(mta -> mta.getMetadata()
                                          .getId()
                                          .equalsIgnoreCase(name))
                        .collect(Collectors.toList());
    }

    public List<DeployedMta> findDeployedMtasByNamespace(String namespace) {
        return getMtas().stream()
                        .filter(mta -> mta.getMetadata()
                                          .hasSameNamespace(namespace))
                        .collect(Collectors.toList());
    }

    public DeployedMta findDeployedMta(String namespace, String name) {

        return getMtas().stream()
                        .filter(mta -> mta.getMetadata()
                                          .hasSameNamespace(namespace)
                            && mta.getMetadata()
                                  .getId()
                                  .equalsIgnoreCase(name))
                        .findFirst()
                        .orElse(null);
    }

}
