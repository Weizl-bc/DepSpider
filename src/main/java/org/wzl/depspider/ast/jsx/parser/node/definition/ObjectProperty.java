package org.wzl.depspider.ast.jsx.parser.node.definition;

import lombok.Getter;
import lombok.Setter;
import org.wzl.depspider.ast.jsx.visitor.JSXNodeVisitor;
import org.wzl.depspider.ast.jsx.parser.node.NodeType;

@Setter
@Getter
public class ObjectProperty extends Property {

    /**
     * 是否为方法
     * eg. const a = () => {}  return true
     */
    private boolean method;

    private Node key;

    /**
     * const obj = { name: "Tom" }; computed为false
     * const obj = { [key]: "Tom" }; computed为true
     * 即当key为[]括号时，为true
     */
    private boolean computed;

    /**
     * const name = "Tom";
     * const obj = { name }; true
     * const obj = { name: "Tom" }; false
     * 这个字段说明对象的属性 不是简写形式。
     */
    private boolean shorthand;

    private Node value;

    public ObjectProperty(int start, int end, Loc loc) {
        super(NodeType.OBJECT_PROPERTY, start, end, loc);
    }

    @Override
    public <T> T accept(JSXNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
