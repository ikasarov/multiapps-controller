package com.sap.cloud.lm.sl.cf.core.validators.parameters;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.sap.cloud.lm.sl.cf.core.Constants;

public interface ParameterValidator {
    
    default boolean isValid(Object parameter, final Map<String, Object> relatedParameters) {
        return true;
    }

    default boolean canCorrect() {
        return false;
    }
    
    default Object attemptToCorrect(Object parameter, final Map<String, Object> relatedParameters) {
        throw new UnsupportedOperationException();
    }

    default boolean containsXsaPlaceholders(Object parameter) {
        if (parameter instanceof String) {
            return ((String) parameter).matches(Constants.PARAMETER_CONTAINING_XSA_PLACEHOLDER_PATTERN);
        }

        return false;
    }
    
    default Set<String> getRelatedParameterNames() {
        return Collections.EMPTY_SET;
    }
    
    String getParameterName();

    Class<?> getContainerType();

}
