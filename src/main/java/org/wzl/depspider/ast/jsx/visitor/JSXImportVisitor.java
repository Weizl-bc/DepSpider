package org.wzl.depspider.ast.jsx.visitor;

import lombok.Getter;
import org.wzl.depspider.ast.jsx.parser.node.FileNode;
import org.wzl.depspider.ast.jsx.parser.node.ProgramNode;
import org.wzl.depspider.ast.jsx.parser.node.definition.ArrayExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.ArrowFunctionExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.CallExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.Identifier;
import org.wzl.depspider.ast.jsx.parser.node.definition.ImportExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.MemberExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.Node;
import org.wzl.depspider.ast.jsx.parser.node.definition.ObjectExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.ObjectProperty;
import org.wzl.depspider.ast.jsx.parser.node.definition.declaration.ExportDefaultDeclaration;
import org.wzl.depspider.ast.jsx.parser.node.definition.declaration.ImportDeclarationNode;
import org.wzl.depspider.ast.jsx.parser.node.definition.declaration.VariableDeclarationNode;
import org.wzl.depspider.ast.jsx.parser.node.definition.literal.NumericLiteral;
import org.wzl.depspider.ast.jsx.parser.node.definition.specifier.ImportSpecifier;
import org.wzl.depspider.ast.jsx.parser.node.definition.specifier.Specifier;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于访问 AST，收集所有 import 路径及其导入的标识符
 *
 * @author weizhilong
 */
@Getter
public class JSXImportVisitor implements JSXNodeVisitor<Void> {

    /**
     * 例如 import { useState, useEffect } from 'react';
     * 则 sourcePath 为 'react'， importedNames 为 [ 'useState', 'useEffect' ]
     */
    public static class ImportRecord {
        public String sourcePath;
        public List<String> importedNames;

        public ImportRecord(String sourcePath) {
            this.sourcePath = sourcePath;
            this.importedNames = new ArrayList<>();
        }

        @Override
        public String toString() {
            return "Import from '" + sourcePath + "': " + importedNames;
        }
    }

    private final List<ImportRecord> imports = new ArrayList<>();

    @Override
    public Void visit(FileNode node) {
        node.getProgram().accept(this);
        return null;
    }

    @Override
    public Void visit(ProgramNode node) {
        for (Node child : node.getBody()) {
            child.accept(this);
        }
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

    @Override
    public Void visit(ImportDeclarationNode node) {
        if (node.getSource() == null) {
            return null;
        }
        String importSource = node.getSource().getValue();
        List<String> importItems = new ArrayList<>();
        for (Specifier specifier : node.getSpecifiers()) {
            ImportSpecifier importSpecifier = (ImportSpecifier) specifier;
            Identifier imported = importSpecifier.getImported();
            String name = imported.getName();
            importItems.add(name);
            specifier.accept(this);
        }
        ImportRecord importRecord = new ImportRecord(importSource);
        importRecord.importedNames = importItems;
        imports.add(importRecord);
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

}
