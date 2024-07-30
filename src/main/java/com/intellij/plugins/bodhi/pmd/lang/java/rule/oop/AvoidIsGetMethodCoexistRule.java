package com.intellij.plugins.bodhi.pmd.lang.java.rule.oop;

import com.intellij.plugins.bodhi.pmd.lang.java.rule.naming.AbstractLuBanRule;
import net.sourceforge.pmd.lang.ast.NodeStream;
import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.ast.internal.JavaAstUtils;
import net.sourceforge.pmd.lang.java.rule.internal.JavaRuleUtil;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AvoidIsGetMethodCoexistRule extends AbstractLuBanRule {
    public AvoidIsGetMethodCoexistRule() {
        super(ASTClassDeclaration.class);
    }

    @Override
    public Object visit(ASTClassDeclaration node, Object data) {
        NodeStream<ASTMethodDeclaration> declarations = node.getDeclarations(ASTMethodDeclaration.class);
        Map<String, List<@NonNull ASTMethodDeclaration>> map = declarations.toStream()
                .filter(JavaRuleUtil::isGetterOrSetter)
                .collect(Collectors.groupingBy(declaration -> declaration.getName().replace("is", "").replace("get", "")));
        map.forEach((k, methods) -> {
            if (methods.size() > 1) {
                methods.forEach(method -> {
                    addViolation(data,method, method.getName());
                });
            }
        });
        return super.visit(node, data);
    }


}
