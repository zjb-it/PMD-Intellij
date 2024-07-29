package com.intellij.plugins.bodhi.pmd.lang.java.rule.comment;

import com.intellij.plugins.bodhi.pmd.lang.java.rule.naming.AbstractLuBanRule;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.document.Chars;
import net.sourceforge.pmd.lang.document.FileLocation;
import net.sourceforge.pmd.lang.document.TextRegion;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.JavaComment;
import net.sourceforge.pmd.lang.ecmascript.ast.ASTComment;
import net.sourceforge.pmd.lang.rule.xpath.CommentNode;
import net.sourceforge.pmd.util.DataMap;
import org.mozilla.javascript.ast.Comment;

import java.util.List;

public class EndLineCommentMustHaveOneWhitespaceRule extends AbstractLuBanRule {

    public EndLineCommentMustHaveOneWhitespaceRule() {
        super(ASTCompilationUnit.class);
    }

    @Override
    public Object visit(ASTCompilationUnit node, Object data) {
        List<JavaComment> comments = node.getComments();
        for (JavaComment comment : comments) {
            if (comment.isSingleLine()) {
                Chars commentText = comment.getText();
                Chars text = commentText.removePrefix("//");
                if (text.startsWith("  ")) {
                    FileLocation reportLocation = comment.getReportLocation();
                    asCtx(data).addViolationWithPosition(node, reportLocation.getStartLine(), reportLocation.getEndLine(), getMessage(),commentText);
                }

            }
        }
        return super.visit(node, data);
    }
}
