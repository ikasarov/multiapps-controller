package com.sap.cloud.lm.sl.cf.core.validators.parameters;

import java.util.Map;

import com.sap.cloud.lm.sl.cf.core.message.Messages;
import com.sap.cloud.lm.sl.cf.core.model.SupportedParameters;
import com.sap.cloud.lm.sl.cf.core.util.NameUtil;
import com.sap.cloud.lm.sl.common.ContentException;
import com.sap.cloud.lm.sl.mta.model.Resource;

public class ServiceNameValidator implements ParameterValidator {

    private final String namespace;
    private final boolean applyNamespaceGlobal;

    public ServiceNameValidator(String namespace, boolean applyNamespace) {
        this.namespace = namespace;
        this.applyNamespaceGlobal = applyNamespace;
    }

    @Override
    public Class<?> getContainerType() {
        return Resource.class;
    }

    @Override
    public String getParameterName() {
        return SupportedParameters.SERVICE_NAME;
    }

    @Override
    public boolean isValid(Object serviceName, final Map<String, Object> context) {
        // The value supplied by the user must always be corrected.
        return false;
    }

    @Override
    public boolean canCorrect() {
        return true;
    }

    @Override
    public Object attemptToCorrect(Object serviceName, final Map<String, Object> relatedParameters) {
        if (!(serviceName instanceof String)) {
            throw new ContentException(Messages.COULD_NOT_CREATE_VALID_SERVICE_NAME_FROM_0, serviceName);
        }

        Object applyNamespaceParameter = relatedParameters.get(SupportedParameters.APPLY_NAMESPACE);

        if (applyNamespaceParameter != null && !(applyNamespaceParameter instanceof Boolean)) {
            throw new ContentException(Messages.COULD_NOT_PARSE_APPLY_NAMESPACE);
        }

        boolean resolvedAppyNamespace = NameUtil.resolveApplyNamespaceFlag(applyNamespaceGlobal, (Boolean) applyNamespaceParameter);

        return NameUtil.computeValidServiceName((String) serviceName, namespace, resolvedAppyNamespace);
    }

}
