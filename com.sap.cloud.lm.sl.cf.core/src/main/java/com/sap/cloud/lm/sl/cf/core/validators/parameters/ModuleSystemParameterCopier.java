package com.sap.cloud.lm.sl.cf.core.validators.parameters;

import java.util.Collections;

import com.sap.cloud.lm.sl.cf.core.model.Parameter;
import com.sap.cloud.lm.sl.mta.model.SystemParameters;
import com.sap.cloud.lm.sl.mta.model.v1.Module;

public class ModuleSystemParameterCopier implements ParameterValidator {

    private Parameter parameter;
    private SystemParameters systemParameters;

    public ModuleSystemParameterCopier(Parameter parameter, SystemParameters systemParameters) {
        this.parameter = parameter;
        this.systemParameters = systemParameters;
    }

    @Override
    public String attemptToCorrect(Object container, Object appName) {
        return getModuleSystemParameter(((Module) container).getName());
    }

    @Override
    public boolean isValid(Object container, Object appName) {
        return getModuleSystemParameter(((Module) container).getName()).equals(appName);
    }

    @Override
    public Parameter getParameter() {
        return parameter;
    }

    @Override
    public Class<Module> getContainerType() {
        return Module.class;
    }

    @Override
    public boolean canCorrect() {
        return true;
    }

    @SuppressWarnings("unchecked")
    private <T> T getModuleSystemParameter(String moduleName) {
        return (T) systemParameters.getModuleParameters()
            .getOrDefault(moduleName, Collections.emptyMap())
            .get(parameter.getName());
    }

}
