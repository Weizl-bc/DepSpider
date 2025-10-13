package org.wzl.depspider.ast.jsx.parser.node.definition;

import lombok.Getter;
import lombok.Setter;
import org.wzl.depspider.ast.jsx.visitor.JSXNodeVisitor;
import org.wzl.depspider.ast.jsx.parser.node.NodeType;

import java.util.List;

@Getter
@Setter
public class ArrowFunctionExpression extends Expression {

    private Identifier id;

    private List<Node> params;

    private Node body;

    private boolean generator;

    private boolean async;

    public ArrowFunctionExpression(int start, int end, Loc loc) {
        super(NodeType.ARROW_FUNCTION_EXPRESSION, start, end, loc);
    }

    @Override
    public <T> T accept(JSXNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

