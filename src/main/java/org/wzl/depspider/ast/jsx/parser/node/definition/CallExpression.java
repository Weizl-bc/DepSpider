package org.wzl.depspider.ast.jsx.parser.node.definition;

import lombok.Getter;
import lombok.Setter;
import org.wzl.depspider.ast.jsx.visitor.JSXNodeVisitor;
import org.wzl.depspider.ast.jsx.parser.node.NodeType;

import java.util.List;

@Getter
@Setter
public class CallExpression extends Expression {

    private Expression callee;

    private List<Expression> arguments;

    public CallExpression(int start, int end, Loc loc) {
        super(NodeType.CALL_EXPRESSION, start, end, loc);
    }

    @Override
    public <T> T accept(JSXNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

