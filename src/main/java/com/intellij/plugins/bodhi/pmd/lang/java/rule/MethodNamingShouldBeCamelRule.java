package com.intellij.plugins.bodhi.pmd.lang.java.rule;

import net.sourceforge.pmd.lang.java.rule.codestyle.ClassNamingConventionsRule;
import net.sourceforge.pmd.lang.java.rule.codestyle.MethodNamingConventionsRule;
import net.sourceforge.pmd.properties.PropertyDescriptor;

import java.util.Objects;
import java.util.regex.Pattern;

public class MethodNamingShouldBeCamelRule extends MethodNamingConventionsRule {


    @Override
    public String getMessage() {
        return AbstractBaseRule.getMessage(this);
    }


}
