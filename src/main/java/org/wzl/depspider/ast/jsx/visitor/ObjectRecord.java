package org.wzl.depspider.ast.jsx.visitor;

import org.wzl.depspider.ast.jsx.parser.node.definition.ObjectExpression;

/**
 * 记录遍历过程中发现的对象表达式。
 */
public final class ObjectRecord {
    private final String path;
    private final ObjectExpression expression;

    public ObjectRecord(String path, ObjectExpression expression) {
        this.path = path;
        this.expression = expression;
    }

    public String getPath() {
        return path;
    }

    public ObjectExpression getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        String prefix = (path == null || path.isEmpty()) ? "<anonymous>" : path;
        return prefix + " -> ObjectExpression";
    }
}
