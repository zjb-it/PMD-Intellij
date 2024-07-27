package com.intellij.plugins.bodhi.pmd.lang.java.rule;

import net.sourceforge.pmd.lang.java.ast.ASTClassDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTClassType;
import net.sourceforge.pmd.lang.java.rule.codestyle.ClassNamingConventionsRule;

import java.util.Objects;

public class ExceptionClassNamingEndWithBaseExceptionRule extends ClassNamingConventionsRule {

    @Override
    public Object visit(ASTClassDeclaration node, Object data) {
//        todo 没解决 Ex1 extends Ex extends RuntimeException 中 EX1的校验问题
        ASTClassType superClassTypeNode = node.getSuperClassTypeNode();
        if (Objects.equals(superClassTypeNode.getSimpleName(), RuntimeException.class.getSimpleName())) {
            if (node.getSimpleName().endsWith("Exception")) {
                asCtx(data).addViolation(node, superClassTypeNode.getSimpleName());
            }
        }
        return super.visit(node, data);
    }



    @Override
    public String getMessage() {
        return AbstractLuBanRule.getMessage(this);
    }
}
