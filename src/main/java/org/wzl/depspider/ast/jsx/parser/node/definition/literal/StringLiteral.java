package org.wzl.depspider.ast.jsx.parser.node.definition.literal;

import lombok.Getter;
import lombok.Setter;
import org.wzl.depspider.ast.jsx.visitor.JSXNodeVisitor;
import org.wzl.depspider.ast.jsx.parser.node.NodeType;
import org.wzl.depspider.ast.jsx.parser.node.definition.Extra;
import org.wzl.depspider.ast.jsx.parser.node.definition.Loc;

@Getter
@Setter
public class StringLiteral extends Literal {

    private String type;
    private int start;
    private int end;
    private Loc loc;
    private Extra extra;
    private String value;

    public StringLiteral(int start, int end,
                         Loc loc, Extra extra,
                         String value) {
        super(NodeType.STRING_LITERAL, start, end, loc);
        this.extra = extra;
        this.value = value;
        this.start = start;
        this.end = end;
    }

    @Override
    public <T> T accept(JSXNodeVisitor<T> visitor) {
        return null;
    }
}