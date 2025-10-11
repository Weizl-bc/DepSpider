package org.wzl.depspider.ast.jsx.parser.node.definition;

import lombok.Getter;
import lombok.Setter;
import org.wzl.depspider.ast.jsx.visitor.JSXNodeVisitor;
import org.wzl.depspider.ast.jsx.parser.node.NodeType;

@Getter
@Setter
public class Identifier extends Expression {
    private String type;
    private int start;
    private int end;
    private Loc loc;
    private String name;

    public Identifier(int start, int end, Loc loc, String name) {
        super(NodeType.IDENTIFIER, start, end, loc);
        this.name = name;
    }

    @Override
    public <T> T accept(JSXNodeVisitor<T> visitor) {
        return null;
    }
}
