package com.intellij.plugins.bodhi.pmd.lang.java.rule.oop;

import com.intellij.plugins.bodhi.pmd.lang.java.rule.naming.AbstractLuBanRule;
import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.types.TypeTestUtil;

public class AvoidJavaSqlDateRule extends AbstractLuBanRule {
    public AvoidJavaSqlDateRule() {
        super(ASTVariableId.class,ASTConstructorCall.class);
    }


    @Override
    public Object visit(ASTVariableId node, Object data) {
        ASTType typeNode = node.getTypeNode();
        if (TypeTestUtil.isA(java.sql.Date.class, typeNode)|| TypeTestUtil.isA(java.sql.Time.class, typeNode) || TypeTestUtil.isA(java.sql.Timestamp.class, typeNode)) {
            addViolation(data,node,node.getName());
        }
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTConstructorCall node, Object data) {
        ASTType typeNode = node.getTypeNode();
        if (TypeTestUtil.isA(java.sql.Date.class, typeNode)|| TypeTestUtil.isA(java.sql.Time.class, typeNode) || TypeTestUtil.isA(java.sql.Timestamp.class, typeNode)) {
            addViolation(data,node,node.getText().toString());
        }
        return super.visit(node, data);
    }
}
