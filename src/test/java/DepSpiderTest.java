import org.junit.Test;
import org.wzl.depspider.ast.core.node.ASTVisitor;
import org.wzl.depspider.ast.jsx.parser.JSXParse;
import org.wzl.depspider.ast.jsx.parser.node.FileNode;
import org.wzl.depspider.ast.jsx.visitor.JSXNodeVisitor;
import org.wzl.depspider.ast.jsx.visitor.JSXObjectVisitor;
import org.wzl.depspider.react.dto.ProjectFileRelation;
import org.wzl.depspider.react.project.IReactProjectOperator;
import org.wzl.depspider.react.project.ReactProjectOperator;
import org.wzl.depspider.react.project.config.ProjectConfiguration;
import org.wzl.depspider.react.project.config.language.Language;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DepSpiderTest {

    @Test
    public void test() {
        String filePath = "/Users/weizhilong/Desktop/DepSpiderDemo/control-tower-hermes-master";
        ProjectConfiguration projectConfiguration = new ProjectConfiguration();

        Set<Language> languages = new HashSet<>();
        languages.add(Language.JS);
        languages.add(Language.TS);
        projectConfiguration.setLanguages(languages);

        IReactProjectOperator reactProjectOperator = new ReactProjectOperator(
                filePath, projectConfiguration
        );

        List<ProjectFileRelation> projectFileRelations = reactProjectOperator.jsxFileRelation();

        List<ProjectFileRelation> collect = projectFileRelations.stream()
                .filter(p -> !p.getRelationFilePaths().isEmpty())
                .collect(Collectors.toList());

        System.out.println(projectConfiguration);
    }

    @Test
    public void t1() {
        String filePath = "/Users/weizhilong/Desktop/DepSpiderDemo/control-tower-hermes-master";
        ProjectConfiguration projectConfiguration = new ProjectConfiguration();

        Set<Language> languages = new HashSet<>();
        languages.add(Language.JS);
        languages.add(Language.TS);
        projectConfiguration.setLanguages(languages);

        IReactProjectOperator reactProjectOperator = new ReactProjectOperator(
                filePath, projectConfiguration
        );

        String packageJsonString = reactProjectOperator.getPackageJsonString();

        System.out.println(packageJsonString);
    }

    @Test
    public void t2() {
//        String indexPath = "/Users/weizhilong/Desktop/DepSpiderDemo/yinhe/src/routes/index.js";
//        String indexPath = "/Users/weizhilong/Desktop/DepSpiderDemo/control-tower-luban-master/src/configs/router-config.js";
        String indexPath = "/Users/weizhilong/Desktop/DepSpiderDemo/site-manager-mix-master/src/routes/bee.js";
        JSXParse jsxParse = new JSXParse(indexPath);
        FileNode parse = jsxParse.parse();
        JSXNodeVisitor<Void> astVisitor = new JSXObjectVisitor();
        astVisitor.visit(parse);
        System.out.println(parse);
    }

}
