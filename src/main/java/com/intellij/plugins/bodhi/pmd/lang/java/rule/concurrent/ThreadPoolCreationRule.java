
package com.intellij.plugins.bodhi.pmd.lang.java.rule.concurrent;

import com.intellij.plugins.bodhi.pmd.lang.java.rule.naming.AbstractLuBanRule;
import net.sourceforge.pmd.lang.java.ast.ASTMethodCall;
import net.sourceforge.pmd.lang.java.types.TypeTestUtil;

import java.util.Set;
import java.util.concurrent.Executors;

public class ThreadPoolCreationRule extends AbstractLuBanRule {

    private static final Set<String> CHECK_METHOD_NAME = Set.of("newFixedThreadPool","newCachedThreadPool","newSingleThreadExecutor");
    public ThreadPoolCreationRule() {
        super(ASTMethodCall.class);
    }

    @Override
    public Object visit(ASTMethodCall node, Object data) {
        if (TypeTestUtil.isA(Executors.class, node.getMethodType().getDeclaringType())) {
            if (CHECK_METHOD_NAME.contains(node.getMethodName())) {
                addViolation(data,node,node.getMethodName());
            }
        }
        return super.visit(node, data);
    }
}
