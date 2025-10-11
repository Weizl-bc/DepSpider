package org.wzl.depspider.ast.jsx.parser.node.definition;

import lombok.Getter;
import lombok.Setter;
import org.wzl.depspider.ast.jsx.visitor.JSXNodeVisitor;
import org.wzl.depspider.ast.jsx.parser.node.NodeType;

import java.util.List;

/**
 * 数组表达式
 */
@Getter
@Setter
public class ArrayExpression extends Expression {

    private List<Expression> elements;

    public ArrayExpression(int start, int end, Loc loc) {
        super(NodeType.ARRAY_EXPRESSION, start, end, loc);
    }

    @Override
    public <T> T accept(JSXNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
