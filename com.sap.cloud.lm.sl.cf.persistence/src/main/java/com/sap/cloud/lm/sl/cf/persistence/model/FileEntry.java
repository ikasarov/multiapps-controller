package com.sap.cloud.lm.sl.cf.persistence.model;

import java.math.BigInteger;
import java.util.Date;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableFileEntry.class)
@JsonDeserialize(as = ImmutableFileEntry.class)
public interface FileEntry {

    @Nullable
    String getId();

    @Nullable
    String getName();

    @Nullable
    String getServiceId();

    @Nullable
    String getSpace();

//    @Nullable
//    String getNamespace();

    @Nullable
    BigInteger getSize();

    @Nullable
    String getDigest();

    @Nullable
    String getDigestAlgorithm();

    @Nullable
    Date getModified();

}
