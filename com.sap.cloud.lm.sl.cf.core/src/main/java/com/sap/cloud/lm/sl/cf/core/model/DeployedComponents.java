package com.sap.cloud.lm.sl.cf.core.model;

import java.util.List;
import java.util.stream.Collectors;

import com.sap.cloud.lm.sl.cf.web.api.model.Mta;

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

    public List<DeployedMta> getMtasWithoutNamespace() {
        return getMtas().stream()
                        .filter(mta -> mta.getMetadata()
                                          .hasNamespace(null))
                        .collect(Collectors.toList());
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
                                          .hasNamespace(namespace))
                        .collect(Collectors.toList());
    }

    public DeployedMta findDeployedMtaByNameAndNamespace(String namespace, String name) {

        return getMtas().stream()
                        .filter(mta -> mta.getMetadata()
                                          .getId()
                                          .equalsIgnoreCase(name)
                            && mta.getMetadata()
                                  .hasNamespace(namespace))
                        .findFirst()
                        .orElse(null);
    }

}
