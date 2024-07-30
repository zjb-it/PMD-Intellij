package com.intellij.plugins.bodhi.pmd.lang.java.rule.oop;

import com.intellij.plugins.bodhi.pmd.lang.java.rule.naming.AbstractLuBanRule;
import net.sourceforge.pmd.lang.ast.NodeStream;
import net.sourceforge.pmd.lang.java.ast.ASTMethodCall;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTTypeExpression;
import net.sourceforge.pmd.lang.java.ast.JavaNode;

public class OverrideMethodMustOverrideAnnotationRule extends AbstractLuBanRule {
    public OverrideMethodMustOverrideAnnotationRule() {
        super(ASTMethodDeclaration.class);
    }

    @Override
    public Object visit(ASTMethodDeclaration node, Object data) {
        if (node.isOverridden()) {
            if (!node.isAnnotationPresent(Override.class)) {
                addViolation(data, node, node.getName());
            }
        }
        return super.visit(node, data);
    }
}
