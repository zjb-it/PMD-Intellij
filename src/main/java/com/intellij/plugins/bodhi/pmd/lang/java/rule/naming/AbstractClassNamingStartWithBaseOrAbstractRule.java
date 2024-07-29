package com.intellij.plugins.bodhi.pmd.lang.java.rule.naming;

import net.sourceforge.pmd.lang.java.ast.ASTClassDeclaration;

public class AbstractClassNamingStartWithBaseOrAbstractRule extends AbstractLuBanRule {

    public AbstractClassNamingStartWithBaseOrAbstractRule() {
        super(ASTClassDeclaration.class);
    }

    @Override
    public Object visit(ASTClassDeclaration node, Object data) {
        if (node.isAbstract()) {
            if (!node.getSimpleName().startsWith("Base") && !node.getSimpleName().startsWith("Abstract")) {
                asCtx(data).addViolation(node,node.getSimpleName());
            }
        }
        return super.visit(node, data);
    }
}
