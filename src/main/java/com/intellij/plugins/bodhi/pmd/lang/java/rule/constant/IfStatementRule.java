package com.intellij.plugins.bodhi.pmd.lang.java.rule.constant;

import com.intellij.plugins.bodhi.pmd.lang.java.rule.naming.AbstractLuBanRule;
import net.sourceforge.pmd.lang.java.ast.ASTIfStatement;

public class IfStatementRule extends AbstractLuBanRule {

    public IfStatementRule() {
        super(ASTIfStatement.class);
    }

    @Override
    public Object visit(ASTIfStatement node, Object data) {
        String text = node.getText().toString();
        String conditionText = node.getCondition().getText().toString();
        if (!text.replace(conditionText,"").startsWith("if ()")) {
            asCtx(data).addViolation(node, "");
        }
        return super.visit(node, data);
    }


}
