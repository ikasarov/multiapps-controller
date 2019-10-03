package com.sap.cloud.lm.sl.cf.web.api.model;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sap.cloud.lm.sl.cf.web.api.AllowNulls;
import com.sap.cloud.lm.sl.cf.web.api.Nullable;
import com.sap.cloud.lm.sl.mta.model.AuditableConfiguration;
import com.sap.cloud.lm.sl.mta.model.ConfigurationIdentifier;

@Value.Immutable
@JsonSerialize(as = ImmutableOperation.class)
@JsonDeserialize(as = ImmutableOperation.class)
public abstract class Operation implements AuditableConfiguration {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;

    @Nullable
    public abstract String getProcessId();

    @Nullable
    @JsonSerialize(using = ProcessTypeSerializer.class)
    @JsonDeserialize(using = ProcessTypeDeserializer.class)
    public abstract ProcessType getProcessType();

    @Nullable
    @JsonSerialize(using = ZonedDateTimeSerializer.class)
    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    public abstract ZonedDateTime getStartedAt();

    @Nullable
    @JsonSerialize(using = ZonedDateTimeSerializer.class)
    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    public abstract ZonedDateTime getEndedAt();

    @Nullable
    public abstract String getSpaceId();

    @Nullable
    public abstract String getMtaId();

    @Nullable
    public abstract String getUser();

    @Nullable
    public abstract Boolean hasAcquiredLock();

    @Nullable
    public abstract State getState();

    @Nullable
    public abstract ErrorType getErrorType();

    public abstract List<Message> getMessages();

    @AllowNulls
    public abstract Map<String, Object> getParameters();

    @Override
    public String getConfigurationType() {
        return "MTA operation";
    }

    @Override
    public String getConfigurationName() {
        return getProcessId();
    }

    @Override
    public List<ConfigurationIdentifier> getConfigurationIdentifiers() {
        List<ConfigurationIdentifier> identifiersList = new ArrayList<>();
        identifiersList.add(new ConfigurationIdentifier("process type", Objects.toString(getProcessType())));
        identifiersList.add(new ConfigurationIdentifier("started at", Objects.toString(getStartedAt())));
        identifiersList.add(new ConfigurationIdentifier("ended at", Objects.toString(getEndedAt())));
        identifiersList.add(new ConfigurationIdentifier("space id", getSpaceId()));
        identifiersList.add(new ConfigurationIdentifier("mta id", getMtaId()));
        identifiersList.add(new ConfigurationIdentifier("user", getUser()));
        identifiersList.add(new ConfigurationIdentifier("state", Objects.toString(getState())));
        identifiersList.add(new ConfigurationIdentifier("error type", Objects.toString(getErrorType())));
        return identifiersList;
    }

}
