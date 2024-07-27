package com.intellij.plugins.bodhi.pmd.lang.java.rule;

import net.sourceforge.pmd.lang.java.ast.ASTVariableId;

import java.util.regex.Pattern;

public class VariableNamingRule extends AbstractLuBanRule {
    static final String CAMEL_CASE = "[a-z][a-zA-Z0-9]*";
    static final Pattern CAMEL_CASE_PATTERN = Pattern.compile(CAMEL_CASE);

    public VariableNamingRule() {
        super(ASTVariableId.class);
    }

    @Override
    public Object visit(ASTVariableId node, Object data) {
        String name = node.getText().toString();
        if (!CAMEL_CASE_PATTERN.matcher(name).matches()) {
            asCtx(data).addViolation(node, name, CAMEL_CASE);
        }
        return data;
    }
}
