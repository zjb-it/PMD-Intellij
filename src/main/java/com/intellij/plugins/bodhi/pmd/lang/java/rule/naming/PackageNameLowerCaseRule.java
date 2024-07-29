package com.intellij.plugins.bodhi.pmd.lang.java.rule.naming;

import net.sourceforge.pmd.lang.java.ast.ASTPackageDeclaration;
import org.apache.commons.lang3.StringUtils;

public class PackageNameLowerCaseRule extends AbstractLuBanRule {
    public PackageNameLowerCaseRule() {
        super(ASTPackageDeclaration.class);
    }

    @Override
    public Object visit(ASTPackageDeclaration node, Object data) {
        String name = node.getName();
        if (!StringUtils.isAllLowerCase(name.replace(".",""))) {
            asCtx(node).addViolation(node, name);
        }
        return data;
    }
}
