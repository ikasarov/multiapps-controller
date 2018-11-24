package com.sap.cloud.lm.sl.cf.core.validators.parameters;

import com.sap.cloud.lm.sl.cf.core.model.Parameter;

public interface ParameterValidator {

    default boolean isValid(Object container, Object parameter) {
        if (parameter != null) {
            return isValid(parameter);
        }
        return true;
    }

    default boolean isValid(Object parameter) {
        return true;
    }

    default boolean canCorrect() {
        return false;
    }

    default Object attemptToCorrect(Object container, Object parameter) {
        return attemptToCorrect(parameter);
    }

    default Object attemptToCorrect(Object parameter) {
        throw new UnsupportedOperationException();
    }

    Class<?> getContainerType();

    Parameter getParameter();

}
