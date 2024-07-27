package com.intellij.plugins.bodhi.pmd.lang.java.rule;


import net.sourceforge.pmd.lang.java.rule.codestyle.FieldNamingConventionsRule;

public class FieldNameRule extends FieldNamingConventionsRule {

//    private static final String CHINESE_REGEX = "[\\u4e00-\\u9fa5]";
//
//    private static final Pattern CHINESE_PATTERN = Pattern.compile(CHINESE_REGEX);
//
//    static final String CAMEL_CASE = "[a-z][a-zA-Z0-9]*";
//    static final Pattern CAMEL_CASE_PATTERN = Pattern.compile(CAMEL_CASE);


    @Override
    public String getMessage() {
        return AbstractLuBanRule.getMessage(this);
    }
}
