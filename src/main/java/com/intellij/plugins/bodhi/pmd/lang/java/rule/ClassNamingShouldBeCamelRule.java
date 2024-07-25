package com.intellij.plugins.bodhi.pmd.lang.java.rule;

import net.sourceforge.pmd.lang.java.ast.ASTClassDeclaration;
import org.checkerframework.checker.nullness.qual.NonNull;

import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.rule.RuleTargetSelector;

import java.util.regex.Pattern;

public class ClassNamingShouldBeCamelRule extends AbstractBaseRule {

    private static final String regex = "^I?([A-Z][a-z0-9]+)+(([A-Z])|(DO|DTO))?$";

    private static final Pattern PATTERN = Pattern.compile(regex);

    @Override
    protected @NonNull RuleTargetSelector buildTargetSelector() {
        return RuleTargetSelector.forTypes(ASTClassDeclaration.class);
    }

    @Override
    public Object visit(ASTClassDeclaration node, Object data) {
        String simpleName = node.getSimpleName();
        if (!PATTERN.matcher(simpleName).matches()) {
            asCtx(data).addViolation(node,simpleName);
        }
        return super.visit(node, data);
    }
}
