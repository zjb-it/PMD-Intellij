package com.intellij.plugins.bodhi.pmd.lang.java.rule;

import net.sourceforge.pmd.lang.java.ast.ASTClassDeclaration;
import net.sourceforge.pmd.lang.java.rule.codestyle.ClassNamingConventionsRule;

public class AbstractClassNamingStartWithBaseOrAbstractRule extends ClassNamingConventionsRule {

    @Override
    public Object visit(ASTClassDeclaration node, Object data) {
        if (node.isAbstract()) {
            if (!node.getSimpleName().startsWith("Base") && !node.getSimpleName().startsWith("Abstract")) {
                asCtx(data).addViolation(node,node.getSimpleName());
            }
        }
        return super.visit(node, data);
    }

    @Override
    public String getMessage() {
        return AbstractLuBanRule.getMessage(this);
    }
}
