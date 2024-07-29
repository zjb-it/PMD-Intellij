package com.intellij.plugins.bodhi.pmd.lang.java.rule.comment;

import com.intellij.plugins.bodhi.pmd.lang.java.rule.naming.AbstractLuBanRule;
import net.sourceforge.pmd.lang.java.ast.*;

public class JavadocCommentRule extends AbstractLuBanRule {

    public JavadocCommentRule() {
        super(ASTClassDeclaration.class, ASTMethodDeclaration.class, ASTFieldDeclaration.class);
    }

    @Override
    public Object visit(ASTClassDeclaration node, Object data) {
        javadocCommentRequired(node,data);
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTMethodDeclaration node, Object data) {
        javadocCommentRequired(node,data);
        return super.visit(node, data);
    }

    /**
     *
     * @param node
     * @param data
     * @return
     */
    @Override
    public Object visit(ASTFieldDeclaration node, Object data) {
        javadocCommentRequired(node,data);
        return super.visit(node, data);
    }

    void javadocCommentRequired(JavadocCommentOwner javadocCommentOwner, Object data) {
        if (javadocCommentOwner.getJavadocComment() == null) {
            asCtx(data).addViolation(javadocCommentOwner);
        }
    }
}
