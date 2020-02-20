package com.sap.cloud.lm.sl.cf.process.steps;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import com.sap.cloud.lm.sl.cf.core.model.CloudTarget;
import com.sap.cloud.lm.sl.cf.core.model.ConfigurationEntry;
import com.sap.cloud.lm.sl.cf.core.persistence.service.ConfigurationEntryService;
import com.sap.cloud.lm.sl.cf.core.util.ConfigurationEntriesUtil;
import com.sap.cloud.lm.sl.cf.process.Constants;
import com.sap.cloud.lm.sl.cf.process.flowable.FlowableFacade;
import com.sap.cloud.lm.sl.cf.process.message.Messages;
import com.sap.cloud.lm.sl.common.util.JsonUtil;

@Named("deleteDiscontinuedConfigurationEntriesStep")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeleteDiscontinuedConfigurationEntriesStep extends SyncFlowableStep {

    @Inject
    private ConfigurationEntryService configurationEntryService;

    @Inject
    private FlowableFacade flowableFacade;

    @Override
    protected StepPhase executeStep(ExecutionWrapper execution) {
        getStepLogger().debug(Messages.DELETING_PUBLISHED_DEPENDENCIES);

        List<ConfigurationEntry> entriesToDelete = getEntriesToDelete(execution.getContext());
        deleteConfigurationEntries(entriesToDelete, execution.getContext());

        getStepLogger().debug(Messages.PUBLISHED_DEPENDENCIES_DELETED);
        return StepPhase.DONE;
    }

    @Override
    protected String getStepErrorMessage(DelegateExecution context) {
        return Messages.ERROR_DELETING_PUBLISHED_DEPENDENCIES;
    }

    private List<ConfigurationEntry> getEntriesToDelete(DelegateExecution context) {
        List<ConfigurationEntry> publishedEntries = StepsUtil.getPublishedEntriesFromSubProcesses(context, flowableFacade);

        List<ConfigurationEntry> allEntriesForCurrentMta = getEntries(context);
        
        List<Long> publishedEntryIds = getEntryIds(publishedEntries);
        
        return allEntriesForCurrentMta.stream()
                                      .filter(entry -> !publishedEntryIds.contains(entry.getId()))
                                      .collect(Collectors.toList());
    }

    private void deleteConfigurationEntries(List<ConfigurationEntry> entriesToDelete, DelegateExecution context) {
        for (ConfigurationEntry entry : entriesToDelete) {
            getStepLogger().info(MessageFormat.format(Messages.DELETING_DISCONTINUED_DEPENDENCY_0, entry.getProviderId()));
            int deletedEntries = configurationEntryService.createQuery()
                                                          .id(entry.getId())
                                                          .delete();
            if (deletedEntries == 0) {
                getStepLogger().warn(Messages.COULD_NOT_DELETE_PROVIDED_DEPENDENCY, entry.getProviderId());
            }
        }
        getStepLogger().debug(Messages.DELETED_ENTRIES, JsonUtil.toJson(entriesToDelete, true));
        StepsUtil.setDeletedEntries(context, entriesToDelete);
    }

    private List<ConfigurationEntry> getEntries(DelegateExecution context) {
        String mtaId = (String) context.getVariable(Constants.PARAM_MTA_ID);
        CloudTarget target = StepsUtil.getCloudTarget(context);
        String namespace = StepsUtil.getNamespace(context);
        
        return configurationEntryService.createQuery()
                                        .providerNid(ConfigurationEntriesUtil.PROVIDER_NID)
                                        .target(target)
                                        .mtaId(mtaId)
                                        .providerNamespace(namespace, true)
                                        .list();
    }

    private List<Long> getEntryIds(List<ConfigurationEntry> configurationEntries) {
        return configurationEntries.stream()
                                   .map(ConfigurationEntry::getId)
                                   .collect(Collectors.toList());
    }

}
