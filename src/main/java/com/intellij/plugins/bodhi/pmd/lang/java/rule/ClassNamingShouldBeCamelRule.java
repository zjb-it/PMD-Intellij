package com.intellij.plugins.bodhi.pmd.lang.java.rule;

import net.sourceforge.pmd.lang.java.ast.ASTClassDeclaration;
import net.sourceforge.pmd.lang.java.rule.codestyle.ClassNamingConventionsRule;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import org.checkerframework.checker.nullness.qual.NonNull;

import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.rule.RuleTargetSelector;

import java.util.Objects;
import java.util.regex.Pattern;

import static com.intellij.plugins.bodhi.pmd.lang.java.rule.AbstractBaseRule.MESSAGES_BUNDLE;

public class ClassNamingShouldBeCamelRule extends ClassNamingConventionsRule {

    private static final String regex = "^I?([A-Z][a-z0-9]+)+(([A-Z])|(DO|DTO))?$";

    private static final Pattern PATTERN = Pattern.compile(regex);


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
        return AbstractBaseRule.getMessage(this);
    }
}
