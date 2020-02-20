package com.sap.cloud.lm.sl.cf.core.model;

import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sap.cloud.lm.sl.cf.core.filters.ContentFilter;
import com.sap.cloud.lm.sl.cf.core.util.ConfigurationEntriesUtil;
import com.sap.cloud.lm.sl.common.model.xml.PropertiesAdapter;

@XmlRootElement(name = "configuration-filter")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class ConfigurationFilter {

    @XmlElement(name = "provider-id")
    private String providerId;
    @XmlElement(name = "required-content")
    @XmlJavaTypeAdapter(value = PropertiesAdapter.class)
    private Map<String, Object> requiredContent;
    @XmlElement(name = "provider-nid")
    private String providerNid;
    @XmlElement(name = "target-space")
    private CloudTarget targetSpace;
    @XmlElement(name = "provider-version")
    private String providerVersion;
    @XmlElement(name = "provider-namespace")
    private String providerNamespace;
    @XmlTransient
    @JsonIgnore
    private boolean strictTargetSpace;

    public ConfigurationFilter() {

    }

    public ConfigurationFilter(String providerNid, String providerId, String providerVersion, String providerNamespace,
                               CloudTarget targetSpace, Map<String, Object> requiredContent) {
        this(providerNid, providerId, providerVersion, providerNamespace, targetSpace, requiredContent, true);
    }

    public ConfigurationFilter(String providerNid, String providerId, String providerVersion, String providerNamespace,
                               CloudTarget targetSpace, Map<String, Object> requiredContent, boolean strictTargetSpace) {
        this.providerNid = providerNid;
        this.providerId = providerId;
        this.providerVersion = providerVersion;
        this.providerNamespace = providerNamespace;
        this.targetSpace = targetSpace;
        this.requiredContent = requiredContent;
        this.strictTargetSpace = strictTargetSpace;
    }

    public String getProviderVersion() {
        return providerVersion;
    }

    public Map<String, Object> getRequiredContent() {
        return requiredContent;
    }

    public String getProviderNid() {
        return providerNid;
    }

    public CloudTarget getTargetSpace() {
        return targetSpace;
    }

    public String getProviderId() {
        return providerId;
    }

    public boolean isStrictTargetSpace() {
        return strictTargetSpace;
    }
    
    public String getProviderNamespace() {
        return providerNamespace;
    }

    private boolean namespaceConstraintIsSatisfied(String providerNamespace) {
        if (this.providerNamespace == null) {
            return true;
        }
        
        if (ConfigurationEntriesUtil.providerNamespaceMustBeEmpty(this.providerNamespace) && providerNamespace == null) {
            return true;
        }
        
        return this.providerNamespace.equals(providerNamespace);
    }

    public boolean matches(ConfigurationEntry entry) {
        if (providerNid != null && !providerNid.equals(entry.getProviderNid())) {
            return false;
        }
        if (targetSpace != null && !targetSpace.equals(entry.getTargetSpace())) {
            return false;
        }
        if (providerId != null && !providerId.equals(entry.getProviderId())) {
            return false;
        }
        if (providerVersion != null && (entry.getProviderVersion() == null || !entry.getProviderVersion()
                                                                                    .satisfies(providerVersion))) {
            return false;
        }
        if (!namespaceConstraintIsSatisfied(entry.getProviderNamespace())) {
            return false;
        }
        return new ContentFilter().test(entry.getContent(), requiredContent);
    }

}
