package org.wzl.depspider.ast.jsx.parser.node.definition.declaration;

import lombok.Getter;
import lombok.Setter;
import org.wzl.depspider.ast.jsx.visitor.JSXNodeVisitor;
import org.wzl.depspider.ast.jsx.parser.node.NodeType;
import org.wzl.depspider.ast.jsx.parser.node.definition.Loc;
import org.wzl.depspider.ast.jsx.parser.node.definition.Node;

@Getter
@Setter
public class ExportDefaultDeclaration extends Node {

    private String exportKind;

    private Node declaration;

    public ExportDefaultDeclaration(int start, int end, Loc loc) {
        super(NodeType.EXPORT_DEFAULT_DECLARATION, start, end, loc);
    }

    @Override
    public <T> T accept(JSXNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
