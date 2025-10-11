package org.wzl.depspider.ast.jsx.visitor;

import org.wzl.depspider.ast.jsx.parser.node.FileNode;
import org.wzl.depspider.ast.jsx.parser.node.ProgramNode;
import org.wzl.depspider.ast.jsx.parser.node.definition.ArrayExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.ArrowFunctionExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.CallExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.ImportExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.MemberExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.ObjectExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.ObjectProperty;
import org.wzl.depspider.ast.jsx.parser.node.definition.declaration.ExportDefaultDeclaration;
import org.wzl.depspider.ast.jsx.parser.node.definition.declaration.ImportDeclarationNode;
import org.wzl.depspider.ast.jsx.parser.node.definition.declaration.VariableDeclarationNode;
import org.wzl.depspider.ast.jsx.parser.node.definition.literal.NumericLiteral;
import org.wzl.depspider.ast.jsx.parser.node.definition.specifier.Specifier;

public class JSXObjectVisitor implements JSXNodeVisitor<Void> {

    @Override
    public Void visit(FileNode fileNode) {
        fileNode.accept(this);
        return null;
    }

    @Override
    public Void visit(ImportDeclarationNode importDeclarationNode) {
        return null;
    }

    @Override
    public Void visit(ExportDefaultDeclaration exportDefaultDeclaration) {
        return null;
    }

    @Override
    public Void visit(Specifier specifier) {
        return null;
    }

    @Override
    public Void visit(ProgramNode programNode) {
        programNode.accept(this);
        return null;
    }

    @Override
    public Void visit(VariableDeclarationNode variableDeclarationNode) {
        return null;
    }

    @Override
    public Void visit(ObjectExpression objectExpression) {
        return null;
    }

    @Override
    public Void visit(ObjectProperty objectProperty) {
        return null;
    }

    @Override
    public Void visit(NumericLiteral numericLiteral) {
        return null;
    }

    @Override
    public Void visit(MemberExpression memberExpression) {
        return null;
    }

    @Override
    public Void visit(ArrayExpression arrayExpression) {
        return null;
    }

    @Override
    public Void visit(CallExpression callExpression) {
        return null;
    }

    @Override
    public Void visit(ArrowFunctionExpression arrowFunctionExpression) {
        return null;
    }

    @Override
    public Void visit(ImportExpression importExpression) {
        return null;
    }
}
