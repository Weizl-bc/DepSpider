package org.wzl.depspider.ast.jsx.parser.node.definition.literal;

import org.wzl.depspider.ast.jsx.visitor.JSXNodeVisitor;
import org.wzl.depspider.ast.jsx.parser.node.NodeType;
import org.wzl.depspider.ast.jsx.parser.node.definition.Loc;

public class NumericLiteral extends Literal {

    public NumericLiteral(int start, int end, Loc loc) {
        super(NodeType.NUMERICL_LITERAL, start, end, loc);
    }

    @Override
    public <T> T accept(JSXNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
