package com.sap.cloud.lm.sl.cf.core.validators.parameters;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.sap.cloud.lm.sl.cf.core.message.Messages;
import com.sap.cloud.lm.sl.cf.core.model.SupportedParameters;
import com.sap.cloud.lm.sl.common.ContentException;
import com.sap.cloud.lm.sl.common.util.MapUtil;
import com.sap.cloud.lm.sl.mta.util.ValidatorUtil;

public class ParametersValidatorHelper {

    private final List<ParameterValidator> parameterValidators;
    private final boolean doNotCorrect;

    public ParametersValidatorHelper(List<ParameterValidator> parameterValidators, boolean doNotCorrect) {
        this.parameterValidators = parameterValidators;
        this.doNotCorrect = doNotCorrect;
    }

    public Map<String, Object> validate(String prefix, Class<?> containerClass, Map<String, Object> parameters) {
        Map<String, Object> correctedParameters = new TreeMap<>();
        for (ParameterValidator validator : parameterValidators) {
            if (!validator.getContainerType()
                          .isAssignableFrom(containerClass)) {
                continue;
            }

            correctInvalidSingleParameters(prefix, validator, parameters, correctedParameters);
            correctInvalidPluralParameters(prefix, validator, parameters, correctedParameters);
        }
        return MapUtil.merge(parameters, correctedParameters);
    }

    private void correctInvalidSingleParameters(String prefix, ParameterValidator validator, Map<String, Object> parameters,
                                                Map<String, Object> correctedParameters) {
        String parameterName = validator.getParameterName();

        Object initialParameterValue = parameters.get(parameterName);
        Object correctParameterValue = validateAndCorrect(ValidatorUtil.getPrefixedName(prefix, parameterName), initialParameterValue,
                                                          validator, parameters);
        if (!Objects.equals(initialParameterValue, correctParameterValue)) {
            correctedParameters.put(parameterName, correctParameterValue);
        }
    }

    private void correctInvalidPluralParameters(String prefix, ParameterValidator validator, Map<String, Object> parameters,
                                                Map<String, Object> correctedParameters) {
        String parameterPluralName = SupportedParameters.SINGULAR_PLURAL_MAPPING.get(validator.getParameterName());

        if (parameterPluralName == null || !parameters.containsKey(parameterPluralName)) {
            return;
        }

        @SuppressWarnings("unchecked")
        List<Object> initialParameterValues = (List<Object>) parameters.get(parameterPluralName);
        if (initialParameterValues == null || initialParameterValues.isEmpty()) {
            return;
        }

        List<Object> correctedParameterValues = initialParameterValues.stream()
                                                                      .map(parameter -> validateAndCorrect(ValidatorUtil.getPrefixedName(prefix,
                                                                                                                                         validator.getParameterName()),
                                                                                                           parameter, validator,
                                                                                                           parameters))
                                                                      .collect(Collectors.toList());
        correctedParameters.put(parameterPluralName, correctedParameterValues);
    }

    private Object validateAndCorrect(String parameterName, Object parameter, ParameterValidator validator,
                                      Map<String, Object> parameters) {
        Map<String, Object> relatedParameters = getRelatedParameters(validator, parameters);

        if (shouldCorrectParameter(parameter, validator, relatedParameters)) {
            return attemptToCorrect(parameterName, parameter, validator, relatedParameters);
        } else {
            return parameter;
        }
    }

    private Map<String, Object> getRelatedParameters(ParameterValidator validator, Map<String, Object> parameters) {
        Set<String> relatedParameterNames = validator.getRelatedParameterNames();

        if (relatedParameterNames.isEmpty()) {
            return Collections.EMPTY_MAP;
        }

        return relatedParameterNames.stream()
                                    .collect(Collectors.toMap(name -> name, name -> parameters.get(name)));
    }

    private boolean shouldCorrectParameter(Object parameter, ParameterValidator validator, Map<String, Object> relatedParameters) {
        return parameter != null && !validator.containsXsaPlaceholders(parameter) && !validator.isValid(parameter, relatedParameters);
    }

    private Object attemptToCorrect(String parameterName, Object parameter, ParameterValidator validator,
                                    Map<String, Object> relatedParameters) {
        if (!validator.canCorrect() || doNotCorrect) {
            throw new ContentException(Messages.CANNOT_CORRECT_PARAMETER, parameterName);
        }
        return validator.attemptToCorrect(parameter, relatedParameters);
    }

}
