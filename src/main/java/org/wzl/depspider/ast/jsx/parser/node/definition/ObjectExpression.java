package org.wzl.depspider.ast.jsx.parser.node.definition;

import lombok.Getter;
import lombok.Setter;
import org.wzl.depspider.ast.jsx.visitor.JSXNodeVisitor;
import org.wzl.depspider.ast.jsx.parser.node.NodeType;

import java.util.List;

/**
 * 对象表达式
 */
@Getter
@Setter
public class ObjectExpression extends Expression {

    /**
     * 每一个对象
     */
    private List<ObjectProperty> properties;

    public ObjectExpression(int start, int end, Loc loc) {
        super(NodeType.OBJECT_EXPRESSION, start, end, loc);
    }

    @Override
    public <T> T accept(JSXNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
