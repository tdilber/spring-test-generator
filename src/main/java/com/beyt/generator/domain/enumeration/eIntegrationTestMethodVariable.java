package com.beyt.generator.domain.enumeration;

/**
 * Created by tdilber at 11/16/2020
 */
public enum eIntegrationTestMethodVariable implements ITemplateVariableEnum {
    MethodName("[methodName]"),
    Entity("[entity]"),
    EntityVariable("[entityVariable]"),
    RepositoryVariable("[repositoryVariable]"),
    DTO("[dto]"),
    DtoVariable("[dtoVariable]"),
    MapperVariable("[mapperVariable]"),
    RequestType("[requestType]"),
    RequestRoute("[requestRoute]"),
    Sets("[sets]"),
    Updates("[updates]"),
    Asserts("[asserts]"),
    Expects("[expects]"),
    WarningMessage("[warningMessage]");

    private String templateVariable;

    eIntegrationTestMethodVariable(String templateVariable) {
        this.templateVariable = templateVariable;
    }

    public String getTemplateVariable() {
        return templateVariable;
    }
}
