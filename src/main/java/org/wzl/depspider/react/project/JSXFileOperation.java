package org.wzl.depspider.react.project;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.wzl.depspider.ast.jsx.parser.JSXParse;
import org.wzl.depspider.ast.jsx.parser.node.FileNode;
import org.wzl.depspider.ast.jsx.parser.node.ProgramNode;
import org.wzl.depspider.ast.jsx.parser.node.definition.Node;
import org.wzl.depspider.ast.jsx.parser.node.definition.declaration.ImportDeclarationNode;
import org.wzl.depspider.ast.jsx.parser.node.definition.specifier.ImportSpecifier;
import org.wzl.depspider.utils.FileUtil;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class JSXFileOperation {

    private final String filePath;

    private final static String FILE_SUFFIX = ".jsx";

    private final JSXParse jsxParse;

    private final FileNode fileNode;

    public JSXFileOperation(String filePath) {
        this.filePath = filePath;

        FileUtil.validateFile(filePath, FILE_SUFFIX);

        this.jsxParse = new JSXParse(filePath);

        this.fileNode = jsxParse.parse();
    }

    @Data
    public static class ImportInfo {
        /**
         * 引入的项目名称
         */
        private List<String> importItems;

        private String source;
    }

    public List<ImportInfo> importInfo() {
        ProgramNode program = fileNode.getProgram();
        List<Node> body = program.getBody();
        return body.stream().map( node -> {
            ImportDeclarationNode importDeclarationNode = (ImportDeclarationNode) node;
            ImportInfo importInfo = new ImportInfo();
            importInfo.setImportItems(
                    importDeclarationNode.getSpecifiers().stream().map(s -> {
                       if (s instanceof ImportSpecifier) {
                           return ((ImportSpecifier) s).getImported().getName();
                       }
                       //TODO 后续处理
                       return "";
                    }).collect(Collectors.toList())
            );
            importInfo.setSource(importDeclarationNode.getSource() == null
                    ? null
                    : importDeclarationNode.getSource().getValue());
            return importInfo;
        }).collect(Collectors.toList());
    }



}
