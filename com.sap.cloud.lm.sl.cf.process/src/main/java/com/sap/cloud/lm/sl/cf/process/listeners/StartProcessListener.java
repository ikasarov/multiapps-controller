package com.sap.cloud.lm.sl.cf.process.listeners;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import javax.inject.Inject;

import org.activiti.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.dao.OngoingOperationDao;
import com.sap.cloud.lm.sl.cf.core.model.OngoingOperation;
import com.sap.cloud.lm.sl.cf.core.model.ProcessType;
import com.sap.cloud.lm.sl.cf.core.util.ConfigurationUtil;
import com.sap.cloud.lm.sl.cf.process.Constants;
import com.sap.cloud.lm.sl.cf.process.message.Messages;
import com.sap.cloud.lm.sl.cf.process.metadata.ProcessTypeToServiceMetadataMapper;
import com.sap.cloud.lm.sl.cf.process.steps.StepsUtil;
import com.sap.cloud.lm.sl.cf.process.util.ProcessTypeParser;
import com.sap.cloud.lm.sl.common.SLException;
import com.sap.cloud.lm.sl.common.util.JsonUtil;
import com.sap.cloud.lm.sl.slp.model.ServiceMetadata;

@Component("startProcessListener")
public class StartProcessListener extends AbstractXS2ProcessExecutionListener {

    private static final long serialVersionUID = -447062578903384602L;

    private static final Logger LOGGER = LoggerFactory.getLogger(StartProcessListener.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;

    @Inject
    private OngoingOperationDao ongoingOperationDao;
    @Inject
    private ProcessTypeParser processTypeParser;
    @Inject
    private ProcessTypeToServiceMetadataMapper processTypeToServiceMetadataMapper;

    @Override
    protected void notifyInternal(DelegateExecution context) throws SLException {
        String correlationId = StepsUtil.getCorrelationId(context);
        if (correlationId == null) {
            correlationId = context.getProcessInstanceId();
            context.setVariable(Constants.VAR_CORRELATION_ID, correlationId);
        }
        ProcessType processType = processTypeParser.getProcessType(context);

        if (ongoingOperationDao.find(correlationId) == null) {
            addOngoingOperation(context, correlationId, processType);
        }
        logProcessEnvironment();
        logProcessVariables(context, processType);
    }

    private void logProcessEnvironment() {
        Map<String, String> environment = ConfigurationUtil.getFilteredEnv();
        getStepLogger().debug(Messages.PROCESS_ENVIRONMENT, JsonUtil.toJson(environment, true));
    }

    private void logProcessVariables(DelegateExecution context, ProcessType processType) {
        ServiceMetadata serviceMetadata = processTypeToServiceMetadataMapper.getServiceMetadata(processType);
        Map<String, Object> nonSensitiveVariables = StepsUtil.getNonSensitiveVariables(context, serviceMetadata);
        getStepLogger().debug(Messages.PROCESS_VARIABLES, JsonUtil.toJson(nonSensitiveVariables, true));
    }

    private void addOngoingOperation(DelegateExecution context, String correlationId, ProcessType processType) {
        String startedAt = FORMATTER.format(ZonedDateTime.now());
        String user = StepsUtil.determineCurrentUser(context, getStepLogger());
        String spaceId = StepsUtil.getSpaceId(context);
        OngoingOperation process = new OngoingOperation(correlationId, processType, startedAt, spaceId, null, user, false, null);
        ongoingOperationDao.add(process);
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}