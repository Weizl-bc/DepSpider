package org.wzl.depspider.ast.jsx.visitor;

import org.wzl.depspider.ast.core.node.ASTVisitor;
import org.wzl.depspider.ast.jsx.parser.node.FileNode;
import org.wzl.depspider.ast.jsx.parser.node.ProgramNode;
import org.wzl.depspider.ast.jsx.parser.node.definition.ArrowFunctionExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.ArrayExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.CallExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.MemberExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.ObjectExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.ObjectProperty;
import org.wzl.depspider.ast.jsx.parser.node.definition.declaration.ExportDefaultDeclaration;
import org.wzl.depspider.ast.jsx.parser.node.definition.declaration.ImportDeclarationNode;
import org.wzl.depspider.ast.jsx.parser.node.definition.declaration.VariableDeclarationNode;
import org.wzl.depspider.ast.jsx.parser.node.definition.literal.NumericLiteral;
import org.wzl.depspider.ast.jsx.parser.node.definition.ImportExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.specifier.Specifier;

/**
 * JSX语法树节点访问器
 * @param <T>   JSX节点Node
 *
 * @author weizhilong
 */
public interface JSXNodeVisitor<T> extends ASTVisitor<T> {

    T visit(FileNode fileNode);

    T visit(ImportDeclarationNode importDeclarationNode);

    T visit(ExportDefaultDeclaration exportDefaultDeclaration);

    T visit(Specifier specifier);

    T visit(ProgramNode programNode);

    T visit(VariableDeclarationNode variableDeclarationNode);

    T visit(ObjectExpression objectExpression);

    T visit(ObjectProperty objectProperty);

    T visit(NumericLiteral numericLiteral);

    T visit(MemberExpression memberExpression);

    T visit(ArrayExpression arrayExpression);

    T visit(CallExpression callExpression);

    T visit(ArrowFunctionExpression arrowFunctionExpression);

    T visit(ImportExpression importExpression);
}
