package com.intellij.plugins.bodhi.pmd.lang.java.rule.flowcontrol;

import com.intellij.plugins.bodhi.pmd.lang.java.rule.naming.AbstractLuBanRule;
import net.sourceforge.pmd.lang.java.ast.*;

import java.util.Objects;

public class BlockMustHavingDelimiterRule extends AbstractLuBanRule {
    public BlockMustHavingDelimiterRule() {
        super(ASTIfStatement.class, ASTForStatement.class,ASTWhileStatement.class,ASTDoStatement.class);
    }
//if/else/for/while/do
    @Override
    public Object visit(ASTIfStatement node, Object data) {
        ASTStatement thenBranch = node.getThenBranch();
        if (!check(thenBranch)) {
            addViolation(data, node, thenBranch.getText());
        }
        ASTStatement elseBranch = node.getElseBranch();
        if (!check(elseBranch)) {
            addViolation(data, node, elseBranch.getText());
        }
        return super.visit(node, data);
    }

    private boolean check(ASTStatement astStatement) {
        if (Objects.nonNull(astStatement)) {
            return astStatement.getFirstToken().imageEquals("{");
        }
        return true;
    }


    @Override
    public Object visit(ASTForStatement node, Object data) {
        check(node, data);
        return super.visit(node, data);
    }

    private void check(ASTLoopStatement node, Object data) {
        ASTStatement body = node.getBody();
        if (!check(body)) {
            addViolation(data, node, body.getText());
        }
    }

    @Override
    public Object visit(ASTWhileStatement node, Object data) {
        check(node, data);
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTDoStatement node, Object data) {
        check(node, data);
        return super.visit(node, data);
    }

    public static void main(String[] args) {
        System.out.println((char)92);
    }
}
