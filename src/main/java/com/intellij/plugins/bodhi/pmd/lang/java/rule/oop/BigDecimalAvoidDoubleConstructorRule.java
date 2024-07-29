package com.intellij.plugins.bodhi.pmd.lang.java.rule.oop;

import com.intellij.plugins.bodhi.pmd.lang.java.rule.naming.AbstractLuBanRule;
import net.sourceforge.pmd.lang.java.ast.ASTConstructorCall;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTNumericLiteral;
import net.sourceforge.pmd.lang.java.types.TypeTestUtil;

import java.math.BigDecimal;

public class BigDecimalAvoidDoubleConstructorRule extends AbstractLuBanRule {
    public BigDecimalAvoidDoubleConstructorRule() {
        super(ASTConstructorCall.class);
    }
//TypeTestUtil.isA(BigDecimal.class,node.getTypeNode())
    @Override
    public Object visit(ASTConstructorCall node, Object data) {
        if (TypeTestUtil.isA(BigDecimal.class, node.getTypeNode())) {
            for (ASTExpression argument : node.getArguments()) {
                if (argument instanceof ASTNumericLiteral numericLiteral && numericLiteral.isDoubleLiteral()) {
                    asCtx(data).addViolation(argument,node.getText().toString());
                }
            }
        }

        return super.visit(node, data);
    }
}
