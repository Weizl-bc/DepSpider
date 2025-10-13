package org.wzl.depspider.ast.jsx.parser.node.definition;

import org.wzl.depspider.ast.jsx.visitor.JSXNodeVisitor;
import org.wzl.depspider.ast.jsx.parser.node.NodeType;

public class ExpressionStatement extends Statement {

    public String type;
    public int start;
    public int end;
    public Loc loc;
    public Expression expression;

    public ExpressionStatement(NodeType nodeType, int start, int end, Loc loc) {
        super(nodeType, start, end, loc);
    }

    @Override
    public <T> T accept(JSXNodeVisitor<T> visitor) {
        return null;
    }
}
