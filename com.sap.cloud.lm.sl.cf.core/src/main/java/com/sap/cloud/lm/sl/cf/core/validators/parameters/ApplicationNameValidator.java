package com.sap.cloud.lm.sl.cf.core.validators.parameters;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.sap.cloud.lm.sl.cf.core.message.Messages;
import com.sap.cloud.lm.sl.cf.core.model.SupportedParameters;
import com.sap.cloud.lm.sl.cf.core.util.NameUtil;
import com.sap.cloud.lm.sl.common.ContentException;
import com.sap.cloud.lm.sl.mta.model.Module;

public class ApplicationNameValidator implements ParameterValidator {

    private final String namespace;
    private final boolean applyNamespaceGlobal;

    public ApplicationNameValidator(String namespace, boolean applyNamespace) {
        this.namespace = namespace;
        this.applyNamespaceGlobal = applyNamespace;
    }

    @Override
    public Class<?> getContainerType() {
        return Module.class;
    }

    @Override
    public String getParameterName() {
        return SupportedParameters.APP_NAME;
    }

    @Override
    public boolean isValid(Object applicationName, final Map<String, Object> context) {
        // The value supplied by the user must always be corrected.
        return false;
    }

    @Override
    public boolean canCorrect() {
        return true;
    }

    @Override
    public Object attemptToCorrect(Object applicationName, final Map<String, Object> relatedParameters) {
        if (!(applicationName instanceof String)) {
            throw new ContentException(Messages.COULD_NOT_CREATE_VALID_APPLICATION_NAME_FROM_0, applicationName);
        }

        Object applyNamespaceParameter = relatedParameters.get(SupportedParameters.APPLY_NAMESPACE);

        if (applyNamespaceParameter != null && !(applyNamespaceParameter instanceof Boolean)) {
            throw new ContentException(Messages.COULD_NOT_PARSE_APPLY_NAMESPACE);
        }

        boolean resolvedAppyNamespace = NameUtil.resolveApplyNamespaceFlag(applyNamespaceGlobal, (Boolean) applyNamespaceParameter);

        return NameUtil.computeValidApplicationName((String) applicationName, namespace, resolvedAppyNamespace);
    }

    @Override
    public Set<String> getRelatedParameterNames() {
        return Collections.singleton(SupportedParameters.APPLY_NAMESPACE);
    }

}
