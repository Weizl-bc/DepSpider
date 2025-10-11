package org.wzl.depspider.ast.jsx.parser.node.definition.declaration;

import lombok.Getter;
import lombok.Setter;
import org.wzl.depspider.ast.jsx.visitor.JSXNodeVisitor;
import org.wzl.depspider.ast.jsx.parser.node.NodeType;
import org.wzl.depspider.ast.jsx.parser.node.definition.Loc;
import org.wzl.depspider.ast.jsx.parser.node.definition.Node;

import java.util.List;

@Getter
@Setter
public class VariableDeclarationNode extends Node {

    private List<VariableDeclarator> declarations;

    public VariableDeclarationNode(int start, int end, Loc loc) {
        super(NodeType.VARIABLE_DECLARATION, start, end, loc);
    }

    @Override
    public <T> T accept(JSXNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
