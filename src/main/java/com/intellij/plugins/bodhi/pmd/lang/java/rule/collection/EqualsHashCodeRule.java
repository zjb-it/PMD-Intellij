package com.intellij.plugins.bodhi.pmd.lang.java.rule.collection;

import com.intellij.plugins.bodhi.pmd.lang.java.rule.naming.AbstractLuBanRule;
import net.sourceforge.pmd.lang.ast.NodeStream;
import net.sourceforge.pmd.lang.java.ast.ASTClassDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;

import java.util.Objects;

public class EqualsHashCodeRule extends AbstractLuBanRule {
    public EqualsHashCodeRule() {
        super(ASTClassDeclaration.class);
    }

    @Override
    public Object visit(ASTClassDeclaration node, Object data) {
        if (node.isRegularClass()) {
            NodeStream<ASTMethodDeclaration> declarations = node.getDeclarations(ASTMethodDeclaration.class);
            if (!declarations.isEmpty()) {
                int count = declarations.filter(method -> Objects.equals(method.getName(), "hashCode") || Objects.equals(method.getName(), "equals")).count();
                if (count == 1) {
                    addViolation(data, node);
                }
            }
        }
        return super.visit(node, data);
    }


}
