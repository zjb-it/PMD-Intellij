package com.intellij.plugins.bodhi.pmd.lang.java.rule.oop;

import com.intellij.plugins.bodhi.pmd.lang.java.rule.naming.AbstractLuBanRule;
import net.sourceforge.pmd.lang.ast.NodeStream;
import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.ast.internal.JavaAstUtils;
import net.sourceforge.pmd.lang.java.rule.internal.JavaRuleUtil;
import net.sourceforge.pmd.lang.java.types.TypeTestUtil;
import net.sourceforge.pmd.lang.rule.xpath.Attribute;

import java.util.Iterator;

public class AvoidAccessStaticViaInstanceRule extends AbstractLuBanRule {
    public AvoidAccessStaticViaInstanceRule() {
        super(ASTMethodCall.class);
    }

    @Override
    public Object visit(ASTMethodCall node, Object data) {
        if (node.getMethodType().isStatic()) {
            NodeStream<JavaNode> children = node.children(ASTTypeExpression.class);
            if (children.isEmpty()) {
                addViolation(node, data, node.getMethodName());
            }
        }
        return super.visit(node, data);
    }


}
