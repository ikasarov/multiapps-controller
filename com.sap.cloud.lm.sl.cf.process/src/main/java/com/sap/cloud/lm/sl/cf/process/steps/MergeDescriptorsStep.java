package com.sap.cloud.lm.sl.cf.process.steps;

import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import com.sap.cloud.lm.sl.cf.core.cf.HandlerFactory;
import com.sap.cloud.lm.sl.cf.core.helpers.MtaDescriptorMerger;
import com.sap.cloud.lm.sl.cf.process.Constants;
import com.sap.cloud.lm.sl.cf.process.message.Messages;
import com.sap.cloud.lm.sl.mta.model.DeploymentDescriptor;
import com.sap.cloud.lm.sl.mta.model.ExtensionDescriptor;
import com.sap.cloud.lm.sl.mta.model.Platform;

@Named("mergeDescriptorsStep")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MergeDescriptorsStep extends SyncFlowableStep {

    protected MtaDescriptorMerger getMtaDescriptorMerger(HandlerFactory factory, Platform platform) {
        return new MtaDescriptorMerger(factory, platform, getStepLogger());
    }

    @Override
    protected StepPhase executeStep(ExecutionWrapper execution) {
        getStepLogger().debug(Messages.MERGING_DESCRIPTORS);
        DeploymentDescriptor deploymentDescriptor = StepsUtil.getDeploymentDescriptor(execution.getContext());
        List<ExtensionDescriptor> extensionDescriptors = StepsUtil.getExtensionDescriptorChain(execution.getContext());

        HandlerFactory handlerFactory = StepsUtil.getHandlerFactory(execution.getContext());
        Platform platform = configuration.getPlatform();
        DeploymentDescriptor descriptor = getMtaDescriptorMerger(handlerFactory, platform).merge(deploymentDescriptor,
                                                                                                 extensionDescriptors);

        StepsUtil.setDeploymentDescriptor(execution.getContext(), descriptor);

        getStepLogger().debug(Messages.DESCRIPTORS_MERGED);
        return StepPhase.DONE;
    }

    // TODO: use this method once namespace is added to descriptors
    private void recalculateNamespace(DelegateExecution context, DeploymentDescriptor descriptor) {

        Map<String, Object> descriptorParameters = descriptor.getParameters();

        if (StepsUtil.getNamespace(context) != null) {
            if (descriptorParameters.containsKey(Constants.PARAM_NAMESPACE)) {
                getStepLogger().debug(Messages.NAMESPACE_IN_DESCRIPTOR_IS_OVERWRITTEN);
            }
            return;
        }

        if (descriptorParameters.containsKey(Constants.PARAM_NAMESPACE)) {
            StepsUtil.setNamespace(context, (String) descriptorParameters.get(Constants.PARAM_NAMESPACE));
        }

        if (descriptorParameters.containsKey(Constants.PARAM_APPLY_NAMESPACE)) {
            StepsUtil.setApplyNamespace(context, (boolean) descriptorParameters.get(Constants.PARAM_APPLY_NAMESPACE));
        }
    }

    @Override
    protected String getStepErrorMessage(DelegateExecution context) {
        return Messages.ERROR_MERGING_DESCRIPTORS;
    }

}
