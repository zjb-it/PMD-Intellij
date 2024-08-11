package com.intellij.plugins.bodhi.pmd.lang.java.rule.base;

import com.intellij.plugins.bodhi.pmd.lang.java.rule.naming.AbstractLuBanRule;
import net.sourceforge.pmd.lang.document.Chars;
import net.sourceforge.pmd.lang.document.FileLocation;
import net.sourceforge.pmd.lang.java.ast.*;

import java.util.List;

public class WrapRule extends AbstractLuBanRule {

    private static final int LINE_MAX_CHAR = 120;

    public WrapRule() {
        super(ASTClassBody.class, ASTExpressionStatement.class);
    }

    @Override
    public Object visit(ASTClassBody node, Object data) {
        node.forEach(body -> {
            if (body instanceof ASTFieldDeclaration fieldDeclaration) {
                int startLine = fieldDeclaration.getReportLocation().getStartLine();
                Chars text = fieldDeclaration.getText();
                check(node, data, text, startLine);
            } else if (body instanceof ASTMethodDeclaration methodDeclaration) {
                FileLocation reportLocation = methodDeclaration.getReportLocation();
                Chars text = methodDeclaration.getText();
                check(node, data, text, reportLocation.getStartLine());
            }
        });

        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTExpressionStatement node, Object data) {
        ASTExpression expr = node.getExpr();
        if (expr instanceof ASTMethodCall methodCall) {
            Chars text = methodCall.getText();
            int beginLine = methodCall.getBeginLine();
            // 不相等，代表多行
            if (beginLine != methodCall.getEndLine()) {
                // 从第2行开始
                List<Chars> collect = text.lineStream().toList();
                for (int i = 1; i < collect.size(); i++) {
                    Chars line = collect.get(i).trimStart();
                    if (!line.startsWith(".")) {
                        asCtx(data).addViolationWithPosition(node, beginLine + i, beginLine + i, getMessage());
                    }
                }

            }
        }
        return super.visit(node, data);
    }

    private void check(ASTClassBody node, Object data, Chars text, int startLine) {
        Iterable<Chars> lines = text.lines();
        int lineNum = 0;
        for (Chars line : lines) {
            if (line.length() > LINE_MAX_CHAR) {
                int beginLine = startLine + lineNum;
                asCtx(data).addViolationWithPosition(node, beginLine, beginLine, getMessage());
            }
            lineNum++;
        }
    }


}
