package org.wzl.depspider.ast.jsx.parser.node.definition.specifier;

import lombok.Getter;
import lombok.Setter;
import org.wzl.depspider.ast.jsx.parser.enumerate.SpecifierType;
import org.wzl.depspider.ast.jsx.visitor.JSXNodeVisitor;
import org.wzl.depspider.ast.jsx.parser.node.NodeType;
import org.wzl.depspider.ast.jsx.parser.node.definition.Loc;
import org.wzl.depspider.ast.jsx.parser.node.definition.Node;

@Setter
@Getter
public class Specifier extends Node {

    private SpecifierType type;

    public Specifier(NodeType nodeType, SpecifierType type, int start, int end, Loc loc) {
        super(nodeType, start, end, loc);
        this.type = type;
    }

    @Override
    public <T> T accept(JSXNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
