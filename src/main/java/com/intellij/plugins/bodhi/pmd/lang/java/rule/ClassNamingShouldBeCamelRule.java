package com.intellij.plugins.bodhi.pmd.lang.java.rule;

import net.sourceforge.pmd.lang.java.ast.ASTClassDeclaration;
import net.sourceforge.pmd.lang.java.rule.codestyle.ClassNamingConventionsRule;
import net.sourceforge.pmd.properties.PropertyDescriptor;

import java.util.Objects;
import java.util.regex.Pattern;

public class ClassNamingShouldBeCamelRule extends ClassNamingConventionsRule {

    private static final String regex = "^I?([A-Z][a-z0-9]+)+(([A-Z])|(DO|DTO))?$";

    private static final Pattern PATTERN = Pattern.compile(regex);




    @Override
    public Object visit(ASTClassDeclaration node, Object data) {
        if (node.isAbstract()) {
            if (!node.getSimpleName().startsWith("Base") && !node.getSimpleName().startsWith("Abstract")) {
                asCtx(data).addViolationWithMessage(node,"（九）【建议】抽象类 {0} 命名使用 Abstract 或 Base 开头",node.getSimpleName());
            }
        }
        return super.visit(node, data);
    }
    @Override
    public boolean isPropertyOverridden(PropertyDescriptor<?> propertyDescriptor) {
        return Objects.equals(propertyDescriptor.name(), "classPattern") && super.isPropertyOverridden(propertyDescriptor);
    }

    @Override
    public <T> T getProperty(PropertyDescriptor<T> propertyDescriptor) {
        if (Objects.equals(propertyDescriptor.name(), "classPattern")) {
            return (T) PATTERN;
        }
        return super.getProperty(propertyDescriptor);
    }

    @Override
    public String getMessage() {
        return AbstractLuBanRule.getMessage(this);
    }
}
