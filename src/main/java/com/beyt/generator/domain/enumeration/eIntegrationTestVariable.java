package com.beyt.generator.domain.enumeration;

/**
 * Created by tdilber at 11/6/2020
 */
public enum eIntegrationTestVariable implements ITemplateVariableEnum {
    PACKAGE("[package]"),
    IMPORTS("[imports]"),
    CLASSNAME("[className]"),
    TODAY("[today]"),
    SPRING_START_CLASS("[springStartClass]"),
    AUTOWIRES("[autowires]"),
    BEFORE_EACH("[beforeEach]"),
    CLASS_BODY("[classBody]");

    private String templateVariable;

    eIntegrationTestVariable(String templateVariable) {
        this.templateVariable = templateVariable;
    }

    public String getTemplateVariable() {
        return templateVariable;
    }
}
