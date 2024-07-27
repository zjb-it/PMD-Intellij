/*
 * Copyright 1999-2017 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
