package org.cloudfoundry.multiapps.controller.process.steps;

import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.cloudfoundry.multiapps.controller.core.util.NameUtil;
import org.cloudfoundry.multiapps.controller.process.Messages;
import org.cloudfoundry.multiapps.controller.process.variables.Variables;
import org.cloudfoundry.multiapps.mta.handlers.HandlerFactory;
import org.cloudfoundry.multiapps.mta.handlers.v2.DescriptorHandler;
import org.cloudfoundry.multiapps.mta.model.DeploymentDescriptor;
import org.cloudfoundry.multiapps.mta.model.Resource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import com.sap.cloudfoundry.client.facade.CloudControllerClient;
import com.sap.cloudfoundry.client.facade.CloudOperationException;

@Named("deleteServiceKeysStep")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeleteServiceKeysStep extends SyncFlowableStep {

    @Override
    protected StepPhase executeStep(ProcessContext context) throws Exception {
        getStepLogger().debug(Messages.DELETING_OLD_SERVICE_KEYS);

        CloudControllerClient client = context.getControllerClient();

        DeploymentDescriptor descriptor = context.getVariable(Variables.COMPLETE_DEPLOYMENT_DESCRIPTOR);
        HandlerFactory handlerFactory = StepsUtil.getHandlerFactory(context.getExecution());
        DescriptorHandler descriptorHandler = handlerFactory.getDescriptorHandler();
        Map<String, List<String>> serviceKeysToDelete = context.getVariable(Variables.SERVICE_KEYS_TO_DELETE);

        for (String resourceName : serviceKeysToDelete.keySet()) {
            Resource resource = descriptorHandler.findResource(descriptor, resourceName);
            if (resource != null) {
                deleteServiceKeysForResource(resource, serviceKeysToDelete.get(resourceName), client);
            }
        }

        return StepPhase.DONE;
    }

    private void deleteServiceKeysForResource(Resource resource, List<String> serviceKeys, CloudControllerClient client) {
        String serviceName = NameUtil.getServiceName(resource);

        getStepLogger().info(Messages.DELETING_OLD_SERVICE_KEYS_FOR_SERVICE, serviceName);

        for (String serviceKey : serviceKeys) {
            try {
                client.deleteServiceKey(serviceName, serviceKey);
            } catch (CloudOperationException e) {
                getStepLogger().warn(e, "Preexisting Service Key marked for deletion was not found after content deploy!");
            }
        }
    }

    @Override
    protected String getStepErrorMessage(ProcessContext context) {
        return Messages.ERROR_DELETING_SERVICE_KEYS;
    }

}
