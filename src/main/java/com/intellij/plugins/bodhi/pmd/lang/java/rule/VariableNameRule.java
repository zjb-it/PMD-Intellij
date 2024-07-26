package com.intellij.plugins.bodhi.pmd.lang.java.rule;

import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTFormalParameter;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclarator;

import java.util.regex.Pattern;

public class VariableNameRule extends AbstractBaseRule {

    private static final String CHINESE_REGEX = "[\\u4e00-\\u9fa5]";

    private static final Pattern CHINESE_PATTERN = Pattern.compile(CHINESE_REGEX);

    static final String CAMEL_CASE = "[a-z][a-zA-Z0-9]*";
    static final Pattern CAMEL_CASE_PATTERN = Pattern.compile(CAMEL_CASE);

    public VariableNameRule() {
        super(ASTVariableDeclarator.class,
                ASTFieldDeclaration.class,
                ASTFormalParameter.class
        );
    }
    @Override
    public Object visit(ASTFormalParameter node, Object data) {
        String simpleName = node.getVarId().getName();
        if (checkPattern(simpleName)) {
            asCtx(data).addViolation(node, simpleName);
        }
        return super.visit(node, data);
    }


    @Override
    public Object visit(ASTVariableDeclarator node, Object data) {
        String simpleName = node.getName();
        if (checkPattern(simpleName)) {
            asCtx(data).addViolation(node, simpleName);
        }
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTFieldDeclaration node, Object data) {
        node.getVarIds().forEach(var -> {
            String name = var.getName();
            if (checkPattern(name)) {
                asCtx(data).addViolation(node, name);
            }
        });
        return super.visit(node, data);
    }


    private static boolean checkPattern(String simpleName) {
        return CHINESE_PATTERN.matcher(simpleName).matches()
                || !CAMEL_CASE_PATTERN.matcher(simpleName).matches()
                || simpleName.startsWith("$")
                || simpleName.startsWith("_")
                || simpleName.endsWith("$")
                || simpleName.endsWith("_")
                || simpleName.endsWith("]");
    }

    public static void main(String[] args) {
        System.out.println("----------------");
        System.out.println(CAMEL_CASE_PATTERN.matcher("get赵DDD").matches());
    }
}
