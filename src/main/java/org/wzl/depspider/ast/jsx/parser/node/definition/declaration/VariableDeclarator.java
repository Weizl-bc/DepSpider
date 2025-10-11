package org.wzl.depspider.ast.jsx.parser.node.definition.declaration;

import lombok.Getter;
import lombok.Setter;
import org.wzl.depspider.ast.jsx.visitor.JSXNodeVisitor;
import org.wzl.depspider.ast.jsx.parser.node.NodeType;
import org.wzl.depspider.ast.jsx.parser.node.definition.Identifier;
import org.wzl.depspider.ast.jsx.parser.node.definition.Loc;
import org.wzl.depspider.ast.jsx.parser.node.definition.Node;

@Getter
@Setter
public class VariableDeclarator extends Declarator {

    /**
     * 变量名
     */
    private Identifier id;

    private Node init;

    /**
     * eg. const let ...
     */
    private String kind;

    public VariableDeclarator(int start, int end, Loc loc) {
        super(NodeType.VARIABLE_DECLARATOR,start, end, loc);
    }

    @Override
    public <T> T accept(JSXNodeVisitor<T> visitor) {
        return null;
    }

}
