package org.wzl.depspider.ast.jsx.parser.node;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.wzl.depspider.ast.jsx.parser.node.definition.CommentBlock;
import org.wzl.depspider.ast.jsx.parser.node.definition.Error;
import org.wzl.depspider.ast.jsx.parser.node.definition.Loc;
import org.wzl.depspider.ast.jsx.parser.node.definition.Node;
import org.wzl.depspider.ast.jsx.visitor.JSXNodeVisitor;

import java.util.List;

/**
 * JSX ast的顶层节点
 *
 * @author weizhilong
 */
@Setter
@Getter
@ToString
public class FileNode extends Node {

    /**
     * 异常
     */
    private List<Error> errors;

    /**
     * 注释行
     */
    private List<CommentBlock> comments;

    /**
     * 文件内部的程序块
     */
    private ProgramNode program;


    public FileNode(int start, int end, Loc loc) {
        super(NodeType.FILE, start, end, loc);
    }

    @Override
    public <T> T accept(JSXNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
