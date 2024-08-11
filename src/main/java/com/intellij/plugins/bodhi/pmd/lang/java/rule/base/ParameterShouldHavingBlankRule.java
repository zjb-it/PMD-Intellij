package com.intellij.plugins.bodhi.pmd.lang.java.rule.base;

import com.intellij.plugins.bodhi.pmd.lang.java.rule.naming.AbstractLuBanRule;
import net.sourceforge.pmd.lang.ast.NodeStream;
import net.sourceforge.pmd.lang.document.Chars;
import net.sourceforge.pmd.lang.java.ast.ASTFormalParameter;
import net.sourceforge.pmd.lang.java.ast.ASTFormalParameters;

import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ParameterShouldHavingBlankRule extends AbstractLuBanRule {

    private static final Pattern REGEX = Pattern.compile(",");

    public ParameterShouldHavingBlankRule() {
        super(ASTFormalParameters.class);
    }


    @Override
    public Object visit(ASTFormalParameters node, Object data) {
        NodeStream<ASTFormalParameter> children = node.children(ASTFormalParameter.class);
        if (children.count() > 1) {
            Chars chars = node.getText().removePrefix("(").removeSuffix(")");
            Iterable<Chars> splits = chars.splits(REGEX);
            StreamSupport.stream(splits.spliterator(), false).skip(1).forEach(arg->{
                if (arg.trimStart().length() + 1 != arg.length()) {
                    addViolation(data, node);
                }
            });
        }
        return super.visit(node, data);
    }
}
