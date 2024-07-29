package com.intellij.plugins.bodhi.pmd.lang.java.rule.naming;


import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTType;
import net.sourceforge.pmd.lang.java.ast.ASTVariableId;
import net.sourceforge.pmd.lang.java.rule.codestyle.FieldNamingConventionsRule;
import net.sourceforge.pmd.lang.java.types.TypeTestUtil;

public class FieldNameRule extends FieldNamingConventionsRule {

//    private static final String CHINESE_REGEX = "[\\u4e00-\\u9fa5]";
//
//    private static final Pattern CHINESE_PATTERN = Pattern.compile(CHINESE_REGEX);
//
//    static final String CAMEL_CASE = "[a-z][a-zA-Z0-9]*";
//    static final Pattern CAMEL_CASE_PATTERN = Pattern.compile(CAMEL_CASE);

    @Override
    public Object visit(ASTFieldDeclaration node, Object data) {
        ASTType typeNode = node.getTypeNode();
        if (TypeTestUtil.isA(boolean.class, typeNode) || TypeTestUtil.isA(Boolean.class, typeNode)) {
            for (ASTVariableId variableId : node) {
                if (variableId.getName().startsWith("is")) {
                    asCtx(data).addViolation(node, variableId.getName(),"","");
                }
            }
        }
        return super.visit(node, data);
    }

    @Override
    public String getMessage() {
        return AbstractLuBanRule.getMessage(this);
    }


}
