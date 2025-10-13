package org.wzl.depspider.ast.jsx.parser.node.definition;

import org.wzl.depspider.ast.jsx.visitor.JSXNodeVisitor;
import org.wzl.depspider.ast.jsx.parser.node.NodeType;

public class ImportExpression extends Expression {

    public ImportExpression(int start, int end, Loc loc) {
        super(NodeType.IMPORT_EXPRESSION, start, end, loc);
    }

    @Override
    public <T> T accept(JSXNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

