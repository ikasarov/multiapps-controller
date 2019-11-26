package com.sap.cloud.lm.sl.cf.core.model;

import java.util.Objects;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sap.cloud.lm.sl.cf.core.Constants;
import com.sap.cloud.lm.sl.cf.core.model.adapter.VersionJsonDeserializer;
import com.sap.cloud.lm.sl.cf.core.model.adapter.VersionJsonSerializer;
import com.sap.cloud.lm.sl.cf.core.model.adapter.VersionXmlAdapter;
import com.sap.cloud.lm.sl.mta.model.Version;

public class DeployedMtaMetadata {

    // In order to keep backwards compatibility the version element cannot be null, since old clients might throw a NPE. TODO: Remove this
    // when compatibility with versions lower than 1.27.3 is not required.
    private static final Version UNKNOWN_MTA_VERSION = Version.parseVersion("0.0.0-unknown");

    private String id;
    private String namespace;
    @JsonSerialize(using = VersionJsonSerializer.class)
    @JsonDeserialize(using = VersionJsonDeserializer.class)
    @XmlJavaTypeAdapter(VersionXmlAdapter.class)
    private Version version;

    public DeployedMtaMetadata() {
    }

    public DeployedMtaMetadata(String id) {
        this(id, null, UNKNOWN_MTA_VERSION);
    }

    public DeployedMtaMetadata(String id, String namespace) {
        this(id, namespace, UNKNOWN_MTA_VERSION);
    }

    public DeployedMtaMetadata(String id, String namespace, Version version) {
        this.id = id;
        this.namespace = namespace;
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public String getNamespace() {
        return namespace;
    }
    
    public boolean hasSameNamespace(String namespace) {
        if (namespace == null) {
            return this.namespace == null;
        }
        
        return namespace.equalsIgnoreCase(this.namespace);
    }
    
    public String getQualifiedId() {
        if (namespace == null) {
            return getId();
        }

        return namespace + Constants.NAMESPACE_SEPARATOR + id;
    }

    public Version getVersion() {
        return version;
    }

    public boolean isVersionUnknown() {
        return version.equals(UNKNOWN_MTA_VERSION);
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, namespace, version);
    }
    
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }
        DeployedMtaMetadata other = (DeployedMtaMetadata) object;
        return Objects.equals(id, other.id) && Objects.equals(namespace, other.namespace) && Objects.equals(version, other.version);
    }

}
