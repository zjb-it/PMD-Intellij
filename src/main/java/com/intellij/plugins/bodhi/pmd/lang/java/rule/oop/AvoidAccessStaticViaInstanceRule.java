package com.intellij.plugins.bodhi.pmd.lang.java.rule.oop;

import com.intellij.plugins.bodhi.pmd.lang.java.rule.naming.AbstractLuBanRule;
import net.sourceforge.pmd.lang.ast.NodeStream;
import net.sourceforge.pmd.lang.java.ast.*;

public class AvoidAccessStaticViaInstanceRule extends AbstractLuBanRule {
    public AvoidAccessStaticViaInstanceRule() {
        super(ASTMethodCall.class, ASTFieldAccess.class);
    }

    @Override
    public Object visit(ASTMethodCall node, Object data) {
        if (node.getMethodType().isStatic()) {
            NodeStream<JavaNode> children = node.children(ASTTypeExpression.class);
            if (children.isEmpty()) {
                addViolation(data, node, node.getMethodName());
            }
        }
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTFieldAccess node, Object data) {
        if (!(node.getQualifier() instanceof ASTTypeExpression)) {
            addViolation(data,node, node.getName());
        }
        return super.visit(node, data);
    }


}
