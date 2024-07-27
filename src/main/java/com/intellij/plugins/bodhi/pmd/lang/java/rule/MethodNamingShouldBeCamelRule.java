package com.intellij.plugins.bodhi.pmd.lang.java.rule;

import net.sourceforge.pmd.lang.java.rule.codestyle.MethodNamingConventionsRule;

public class MethodNamingShouldBeCamelRule extends MethodNamingConventionsRule {


    @Override
    public String getMessage() {
        return AbstractLuBanRule.getMessage(this);
    }


}
