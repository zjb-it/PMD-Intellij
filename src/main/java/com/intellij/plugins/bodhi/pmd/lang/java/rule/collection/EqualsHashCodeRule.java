package com.intellij.plugins.bodhi.pmd.lang.java.rule.collection;

import com.intellij.plugins.bodhi.pmd.lang.java.rule.naming.AbstractLuBanRule;
import net.sourceforge.pmd.lang.java.rule.errorprone.OverrideBothEqualsAndHashcodeRule;

public class EqualsHashCodeRule extends OverrideBothEqualsAndHashcodeRule {

    @Override
    public String getMessage() {
        return AbstractLuBanRule.getMessage(this);
    }
}
