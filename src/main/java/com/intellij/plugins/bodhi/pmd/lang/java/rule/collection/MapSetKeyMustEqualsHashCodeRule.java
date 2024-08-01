package com.intellij.plugins.bodhi.pmd.lang.java.rule.collection;

import com.intellij.plugins.bodhi.pmd.lang.java.rule.naming.AbstractLuBanRule;
import net.sourceforge.pmd.lang.ast.NodeStream;
import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.ast.internal.JavaAstUtils;
import net.sourceforge.pmd.lang.java.rule.internal.JavaRuleUtil;
import net.sourceforge.pmd.lang.java.types.TypeTestUtil;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MapSetKeyMustEqualsHashCodeRule extends AbstractLuBanRule {
    public MapSetKeyMustEqualsHashCodeRule() {
        super(ASTVariableDeclarator.class);
    }

    //((ASTClassType)typeNode.children(ASTTypeArguments.class).children(ASTClassType.class).first())
    @Override
    public Object visit(ASTVariableDeclarator node, Object data) {
        ASTType typeNode = node.getVarId().getTypeNode();
        if (TypeTestUtil.isA(Collection.class, typeNode) || TypeTestUtil.isA(Map.class, typeNode)) {
            ASTClassType classType = typeNode.children(ASTTypeArguments.class).children(ASTClassType.class).first();
            if (!classType.getTypeMirror().unbox().isPrimitive()) {
                long count = classType.getTypeMirror()
                        .streamMethods(method -> Objects.equals(method.getSimpleName(), "hashCode")
                                || Objects.equals(method.getSimpleName(), "equals"))
                        .count();
                if (count == 1) {
                    addViolation(data, node);
                }
            }
        }
        return super.visit(node, data);
    }
}
