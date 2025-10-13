package org.wzl.depspider.example;

import org.wzl.depspider.ast.jsx.parser.JSXParse;
import org.wzl.depspider.ast.jsx.parser.node.FileNode;
import org.wzl.depspider.ast.jsx.parser.node.definition.ObjectExpression;
import org.wzl.depspider.ast.jsx.visitor.JSXObjectVisitor;
import org.wzl.depspider.ast.jsx.visitor.ObjectRecord;

public final class JSXObjectVisitorExample {

    private JSXObjectVisitorExample() {
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java " + JSXObjectVisitorExample.class.getName() + " <path-to-jsx-file>");
            System.exit(1);
        }

        String filePath = args[0];
        JSXParse parser = new JSXParse(filePath);
        FileNode fileNode = parser.parse();

        JSXObjectVisitor visitor = new JSXObjectVisitor();
        visitor.visit(fileNode);

        System.out.println("Discovered object expressions:");
        for (ObjectRecord record : visitor.getObjectRecords()) {
            ObjectExpression expression = record.getExpression();
            String path = record.getPath();
            int propertyCount = expression.getProperties() == null ? 0 : expression.getProperties().size();
            System.out.println("- path=" + (path == null ? "<unknown>" : path)
                    + ", properties=" + propertyCount);
        }
    }
}
