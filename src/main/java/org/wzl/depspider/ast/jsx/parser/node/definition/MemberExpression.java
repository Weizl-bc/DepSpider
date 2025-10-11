package org.wzl.depspider.ast.jsx.parser.node.definition;

import lombok.Getter;
import lombok.Setter;
import org.wzl.depspider.ast.jsx.visitor.JSXNodeVisitor;
import org.wzl.depspider.ast.jsx.parser.node.NodeType;

@Getter
@Setter
public class MemberExpression extends Expression {

    public String type;
    public int start;
    public int end;
    public Loc loc;
    public Expression object;
    public boolean computed;
    public Identifier property;

    public MemberExpression(int start, int end, Loc loc) {
        super(NodeType.MEMBER_EXPRESSION, start, end, loc);
    }

    @Override
    public <T> T accept(JSXNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
