package org.wzl.depspider.ast.jsx.visitor;

import org.wzl.depspider.ast.jsx.parser.node.FileNode;
import org.wzl.depspider.ast.jsx.parser.node.ProgramNode;
import org.wzl.depspider.ast.jsx.parser.node.definition.ArrayExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.ArrowFunctionExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.CallExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.Expression;
import org.wzl.depspider.ast.jsx.parser.node.definition.Identifier;
import org.wzl.depspider.ast.jsx.parser.node.definition.ImportExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.MemberExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.Node;
import org.wzl.depspider.ast.jsx.parser.node.definition.ObjectExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.ObjectProperty;
import org.wzl.depspider.ast.jsx.parser.node.definition.declaration.ExportDefaultDeclaration;
import org.wzl.depspider.ast.jsx.parser.node.definition.declaration.ImportDeclarationNode;
import org.wzl.depspider.ast.jsx.parser.node.definition.declaration.VariableDeclarationNode;
import org.wzl.depspider.ast.jsx.parser.node.definition.declaration.VariableDeclarator;
import org.wzl.depspider.ast.jsx.parser.node.definition.literal.NumericLiteral;
import org.wzl.depspider.ast.jsx.parser.node.definition.literal.StringLiteral;
import org.wzl.depspider.ast.jsx.parser.node.definition.specifier.Specifier;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Visitor 用于搜集 JSX 文件中出现的对象表达式。
 */
public class JSXObjectVisitor implements JSXNodeVisitor<ObjectRecord> {

    private final List<ObjectRecord> objectRecords = new ArrayList<>();
    private final Set<ObjectExpression> visitedExpressions =
            Collections.newSetFromMap(new IdentityHashMap<>());
    private final Deque<String> nameStack = new ArrayDeque<>();

    public List<ObjectRecord> getObjectRecords() {
        return Collections.unmodifiableList(objectRecords);
    }

    public List<ObjectExpression> getObjectExpressions() {
        List<ObjectExpression> expressions = new ArrayList<>(objectRecords.size());
        for (ObjectRecord record : objectRecords) {
            expressions.add(record.getExpression());
        }
        return Collections.unmodifiableList(expressions);
    }

    /**
     * 记录遍历过程中发现的对象表达式。
     */
    public static class ObjectRecord {
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

    private final List<ObjectRecord> objectRecords = new ArrayList<>();
    private final Set<ObjectExpression> visitedExpressions =
            Collections.newSetFromMap(new IdentityHashMap<>());
    private final Deque<String> nameStack = new ArrayDeque<>();

    public List<ObjectRecord> getObjectRecords() {
        return Collections.unmodifiableList(objectRecords);
    }

    public List<ObjectExpression> getObjectExpressions() {
        List<ObjectExpression> expressions = new ArrayList<>(objectRecords.size());
        for (ObjectRecord record : objectRecords) {
            expressions.add(record.getExpression());
        }
        return Collections.unmodifiableList(expressions);
    }

    @Override
    public ObjectRecord visit(FileNode fileNode) {
        if (fileNode == null) {
            return null;
        }
        return visitNode(fileNode.getProgram());
    }

    @Override
    public ObjectRecord visit(ImportDeclarationNode importDeclarationNode) {
        if (importDeclarationNode == null) {
            return null;
        }
        if (importDeclarationNode.getSpecifiers() != null) {
            for (Specifier specifier : importDeclarationNode.getSpecifiers()) {
                visit(specifier);
            }
        }
        return null;
    }

    @Override
    public ObjectRecord visit(ExportDefaultDeclaration exportDefaultDeclaration) {
        if (exportDefaultDeclaration == null) {
            return null;
        }
        Node declaration = exportDefaultDeclaration.getDeclaration();
        if (declaration != null) {
            boolean pushed = pushName("default");
            try {
                visitNode(declaration);
            } finally {
                popName(pushed);
            }
        }
        return null;
    }

    @Override
    public ObjectRecord visit(Specifier specifier) {
        return null;
    }

    @Override
    public ObjectRecord visit(ProgramNode programNode) {
        if (programNode == null || programNode.getBody() == null) {
            return null;
        }
        for (Node child : programNode.getBody()) {
            visitNode(child);
        }
        return null;
    }

    @Override
    public ObjectRecord visit(VariableDeclarationNode variableDeclarationNode) {
        if (variableDeclarationNode == null || variableDeclarationNode.getDeclarations() == null) {
            return null;
        }
        for (VariableDeclarator declarator : variableDeclarationNode.getDeclarations()) {
            if (declarator == null) {
                continue;
            }
            Identifier identifier = declarator.getId();
            String name = identifier != null ? identifier.getName() : null;
            Node init = declarator.getInit();
            boolean pushed = pushName(name);
            try {
                visitNode(init);
            } finally {
                popName(pushed);
            }
        }
        return null;
    }

    @Override
    public ObjectRecord visit(ObjectExpression objectExpression) {
        if (objectExpression == null || !visitedExpressions.add(objectExpression)) {
            return null;
        }
        ObjectRecord record = new ObjectRecord(currentPath(), objectExpression);
        objectRecords.add(record);
        if (objectExpression.getProperties() != null) {
            for (ObjectProperty property : objectExpression.getProperties()) {
                visit(property);
            }
        }
        return record;
    }

    @Override
    public ObjectRecord visit(ObjectProperty objectProperty) {
        if (objectProperty == null) {
            return null;
        }
        String propertyName = resolvePropertyName(objectProperty.getKey(), objectProperty.isComputed());
        Node value = objectProperty.getValue();
        boolean pushed = pushName(propertyName);
        try {
            visitNode(value);
        } finally {
            popName(pushed);
        }
        return null;
    }

    @Override
    public ObjectRecord visit(NumericLiteral numericLiteral) {
        return null;
    }

    @Override
    public ObjectRecord visit(MemberExpression memberExpression) {
        if (memberExpression == null) {
            return null;
        }
        visitNode(memberExpression.getObject());
        return null;
    }

    @Override
    public ObjectRecord visit(ArrayExpression arrayExpression) {
        if (arrayExpression == null || arrayExpression.getElements() == null) {
            return null;
        }
        for (Expression element : arrayExpression.getElements()) {
            visitNode(element);
        }
        return null;
    }

    @Override
    public ObjectRecord visit(CallExpression callExpression) {
        if (callExpression == null) {
            return null;
        }
        visitNode(callExpression.getCallee());
        if (callExpression.getArguments() != null) {
            for (Expression argument : callExpression.getArguments()) {
                visitNode(argument);
            }
        }
        return null;
    }

    @Override
    public ObjectRecord visit(ArrowFunctionExpression arrowFunctionExpression) {
        if (arrowFunctionExpression == null) {
            return null;
        }
        if (arrowFunctionExpression.getParams() != null) {
            for (Node param : arrowFunctionExpression.getParams()) {
                visitNode(param);
            }
        }
        visitNode(arrowFunctionExpression.getBody());
        return null;
    }

    @Override
    public ObjectRecord visit(ImportExpression importExpression) {
        return null;
    }

    private ObjectRecord visitNode(Node node) {
        if (node == null) {
            return null;
        }
        if (node instanceof FileNode) {
            return visit((FileNode) node);
        }
        if (node instanceof ProgramNode) {
            return visit((ProgramNode) node);
        }
        if (node instanceof ImportDeclarationNode) {
            return visit((ImportDeclarationNode) node);
        }
        if (node instanceof ExportDefaultDeclaration) {
            return visit((ExportDefaultDeclaration) node);
        }
        if (node instanceof VariableDeclarationNode) {
            return visit((VariableDeclarationNode) node);
        }
        if (node instanceof ObjectExpression) {
            return visit((ObjectExpression) node);
        }
        if (node instanceof ObjectProperty) {
            return visit((ObjectProperty) node);
        }
        if (node instanceof ArrayExpression) {
            return visit((ArrayExpression) node);
        }
        if (node instanceof CallExpression) {
            return visit((CallExpression) node);
        }
        if (node instanceof ArrowFunctionExpression) {
            return visit((ArrowFunctionExpression) node);
        }
        if (node instanceof MemberExpression) {
            return visit((MemberExpression) node);
        }
        if (node instanceof ImportExpression) {
            return visit((ImportExpression) node);
        }
        if (node instanceof NumericLiteral) {
            return visit((NumericLiteral) node);
        }
        return null;
    }

    private boolean pushName(String name) {
        if (name == null) {
            return false;
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        nameStack.addLast(trimmed);
        return true;
    }

    private void popName(boolean pushed) {
        if (pushed && !nameStack.isEmpty()) {
            nameStack.removeLast();
        }
    }

    private String currentPath() {
        if (nameStack.isEmpty()) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        Iterator<String> iterator = nameStack.iterator();
        while (iterator.hasNext()) {
            String part = iterator.next();
            if (part == null || part.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('.');
            }
            builder.append(part);
        }
        return builder.length() == 0 ? null : builder.toString();
    }

    private String resolvePropertyName(Node key, boolean computed) {
        if (computed || key == null) {
            return null;
        }
        if (key instanceof Identifier) {
            return ((Identifier) key).getName();
        }
        if (key instanceof StringLiteral) {
            String value = ((StringLiteral) key).getValue();
            return stripQuotes(value);
        }
        return null;
    }

    private String stripQuotes(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() >= 2) {
            char first = trimmed.charAt(0);
            char last = trimmed.charAt(trimmed.length() - 1);
            if ((first == '\'' && last == '\'') || (first == '"' && last == '"')) {
                return trimmed.substring(1, trimmed.length() - 1);
            }
        }
        return trimmed;
    }

}
