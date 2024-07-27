
package com.intellij.plugins.bodhi.pmd.lang.java.rule;

import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.rule.internal.TestFrameworksUtil;

public class TestClassShouldEndWithTestNamingRule extends AbstractLuBanRule {
    private static final String TEST_SUFFIX = "Test";

    public TestClassShouldEndWithTestNamingRule() {
        super(ASTMethodDeclaration.class);
    }

    @Override
    public Object visit(ASTMethodDeclaration node, Object data) {
        if (TestFrameworksUtil.isJUnit5Method(node) || TestFrameworksUtil.isJUnit4Method(node) || TestFrameworksUtil.isJUnit3Method(node)) {
            if (!node.getName().endsWith(TEST_SUFFIX)) {
                asCtx(data).addViolation(node, node.getName());
            }
        }
        return super.visit(node, data);
    }
}
