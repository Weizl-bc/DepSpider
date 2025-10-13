package org.wzl.depspider.ast.jsx.parser.node.definition.declaration;


import lombok.Getter;
import lombok.Setter;
import org.wzl.depspider.ast.jsx.visitor.JSXNodeVisitor;
import org.wzl.depspider.ast.jsx.parser.node.NodeType;
import org.wzl.depspider.ast.jsx.parser.node.definition.CommentBlock;
import org.wzl.depspider.ast.jsx.parser.node.definition.Loc;
import org.wzl.depspider.ast.jsx.parser.node.definition.Node;
import org.wzl.depspider.ast.jsx.parser.node.definition.literal.StringLiteral;
import org.wzl.depspider.ast.jsx.parser.node.definition.specifier.Specifier;

import java.util.List;

@Getter
@Setter
public class ImportDeclarationNode extends Node {

    public String type;
    public int start;
    public int end;
    public Loc loc;
    /**
     * params extends Specifier
     * @see Specifier
     */
    public List<Specifier> specifiers;
    public StringLiteral source;
    public List<Object> attributes;
    public List<CommentBlock> leadingComments;

    public ImportDeclarationNode(int start, int end, Loc loc) {
        super(NodeType.IMPORT_DECLARATION, start, end, loc);
    }

    @Override
    public <T> T accept(JSXNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
