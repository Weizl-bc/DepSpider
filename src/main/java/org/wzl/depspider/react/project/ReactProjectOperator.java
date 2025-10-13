package org.wzl.depspider.react.project;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.wzl.depspider.ast.jsx.visitor.JSXImportVisitor;
import org.wzl.depspider.ast.jsx.parser.JSXParse;
import org.wzl.depspider.ast.jsx.parser.node.FileNode;
import org.wzl.depspider.ast.jsx.parser.node.ProgramNode;
import org.wzl.depspider.ast.jsx.parser.node.definition.ArrayExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.ArrowFunctionExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.CallExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.Expression;
import org.wzl.depspider.ast.jsx.parser.node.definition.Identifier;
import org.wzl.depspider.ast.jsx.parser.node.definition.ImportExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.Node;
import org.wzl.depspider.ast.jsx.parser.node.definition.ObjectExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.ObjectProperty;
import org.wzl.depspider.ast.jsx.parser.node.definition.declaration.ExportDefaultDeclaration;
import org.wzl.depspider.ast.jsx.parser.node.definition.declaration.ImportDeclarationNode;
import org.wzl.depspider.ast.jsx.parser.node.definition.declaration.VariableDeclarationNode;
import org.wzl.depspider.ast.jsx.parser.node.definition.declaration.VariableDeclarator;
import org.wzl.depspider.ast.jsx.parser.node.definition.literal.StringLiteral;
import org.wzl.depspider.ast.jsx.parser.node.definition.specifier.ImportSpecifier;
import org.wzl.depspider.ast.jsx.parser.node.definition.specifier.Specifier;
import org.wzl.depspider.react.dto.FileImport;
import org.wzl.depspider.react.dto.FileImportDetail;
import org.wzl.depspider.react.dto.FileRelationDetail;
import org.wzl.depspider.react.dto.PageRouterDefine;
import org.wzl.depspider.react.dto.ProjectFileRelation;
import org.wzl.depspider.react.exception.ReactProjectInitException;
import org.wzl.depspider.react.exception.ReactProjectValidException;
import org.wzl.depspider.react.exception.ScanPathSetException;
import org.wzl.depspider.react.project.config.language.CompositeLanguageStrategy;
import org.wzl.depspider.react.project.config.language.Language;
import org.wzl.depspider.react.project.config.ProjectConfiguration;
import org.wzl.depspider.react.project.config.language.LanguageStrategy;
import org.wzl.depspider.react.project.config.language.LanguageStrategyFactory;
import org.wzl.depspider.utils.FileUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * React 项目操作类
 *
 * @author weizhilong
 */
@Slf4j
public class ReactProjectOperator implements IReactProjectOperator {

    /**
     * 项目根目录
     */
    @Getter
    private final String projectPath;

    /**
     * 项目根目录
     */
    @Getter
    private final File projectFileFolder;

    /**
     * 项目配置
     */
    @Getter
    private final ProjectConfiguration projectConfiguration;

    /**
     * src目录
     */
    @Getter
    private final File srcFileFolder;

    /**
     * src下属的所有目录
     */
    @Getter
    private final List<File> srcFolderChildren;

    /**
     * 扫描路径
     * ProjectConfiguration#scanPath 可配置
     */
    private File scanPath;

    /**
     * 语言策略类
     */
    private LanguageStrategy languageStrategy;

    /**
     * 项目的入口文件，例如index.js、index.jsx、index.tsx等
     * 该文件只包含了是否调用了 ReactDOM.render() 或 root.render()等方法来渲染组件
     */
    private File projectIndexFile;

    /**
     * 构造函数
     * @param projectPath 项目根目录
     * @param projectConfiguration 项目配置
     */
    public ReactProjectOperator(String projectPath,
                                ProjectConfiguration projectConfiguration) {
        this.projectPath = projectPath;
        this.projectFileFolder = new File(projectPath);
        this.projectConfiguration = projectConfiguration;
        this.srcFileFolder = new File(projectPath, "src");
        this.srcFolderChildren = new ArrayList<>();
        for (File file : Objects.requireNonNull(this.srcFileFolder.listFiles())) {
            if (file.isDirectory()) {
                this.srcFolderChildren.add(file);
            }
        }
        log.info("ReactProjectOperator initialized with project path: {}", projectPath);
        initProject();
    }

    private void initProject() {
        setScanPath();
        setLanguageStrategy();
        setProjectIndexFile();
    }

    /**
     * 获取一个项目的入口文件
     */
    private void setProjectIndexFile() {
        for (File file : Objects.requireNonNull(srcFileFolder.listFiles())) {
            String regexp = "(createRoot\\s*\\(|root\\.render\\s*\\(|ReactDOM\\.render\\s*\\(|React\\.createElement\\s*\\(|hydrateRoot\\s*\\(|hydrate\\s*\\()\n";
            String fileContent;
            try {
                fileContent = FileUtil.getInputString(file);
            } catch (IOException e) {
                throw new ReactProjectInitException("读取文件入口失败: " + file.getAbsolutePath());
            }
            Matcher matcher = Pattern.compile(regexp).matcher(fileContent);
            if (matcher.find()) {
                this.projectIndexFile = file;
                return;
            }
        }
    }

    private void setLanguageStrategy() {
        Set<Language> languages = projectConfiguration.getLanguages();
        List<LanguageStrategy> languageStrategies = new ArrayList<>();
        for (Language language : languages) {
            LanguageStrategy languageStrategy1 = LanguageStrategyFactory.getLanguageStrategy(language);
            if (null != languageStrategy1) {
                languageStrategies.add(languageStrategy1);
            }
        }
        if (languageStrategies.isEmpty()) {
            throw new IllegalStateException("No language strategy found");
        } else if (languageStrategies.size() > 1) {
            languageStrategy = new CompositeLanguageStrategy(languageStrategies);
        } else {
            languageStrategy = languageStrategies.get(0);
        }

    }

    private void setScanPath() {
        List<String> projectConfigurationScanPath = projectConfiguration.getScanPath();
        if (null != projectConfigurationScanPath && !projectConfigurationScanPath.isEmpty()) {
            this.scanPath = FileUtil.resolvePath(this.projectFileFolder, projectConfigurationScanPath);
            if (!this.scanPath.isDirectory()) {
                throw new ScanPathSetException("Scan path is not a directory: " + projectConfigurationScanPath);
            }
        } else {
            // 默认从src开始扫描
            this.scanPath = this.srcFileFolder;
        }
    }

    @Override
    public List<ProjectFileRelation> jsxFileRelation() {
        List<FileRelationDetail> fileRelationDetails = this.srcScan();
        return fileRelationDetails.stream().map(f -> {
            ProjectFileRelation projectFileRelation = new ProjectFileRelation();
            projectFileRelation.setRelationFilePaths(f.getRelationFilePaths());
            projectFileRelation.setTargetFile(f.getTargetFile());
            return projectFileRelation;
        }).collect(Collectors.toList());
    }

    @Override
    public List<File> findJsxFileWithImport(Map<String, List<String>> importMap) {
        List<FileRelationDetail> fileRelationDetails = this.srcScan();
        Set<File> files = new HashSet<>();
        for (FileRelationDetail fileRelationDetail : fileRelationDetails) {
            Map<String, List<String>> importedMap = fileRelationDetail.getImportMap();
            for (Map.Entry<String, List<String>> entry : importedMap.entrySet()) {
                String key = entry.getKey();
                boolean find = false;
                if (importMap.containsKey(key)) {
                    List<String> importValues = importMap.get(key);
                    if (null == importValues) {
                        files.add(fileRelationDetail.getTargetFile());
                        break;
                    }
                    List<String> importedValues = importedMap.get(key);
                    for (String importedValue : importedValues) {
                        if (importValues.contains(importedValue)) {
                            files.add(fileRelationDetail.getTargetFile());
                            find = true;
                            break;
                        }
                    }
                    if (find) {
                        break;
                    }
                }
            }
        }
        return new ArrayList<>(files);
    }

    @Override
    public List<ProjectFileRelation> deepSearchProjectRelation(List<ProjectFileRelation> projectFileRelations) {
        Map<File, ProjectFileRelation> targetMap = new HashMap<>();
        Map<File, List<File>> reverseMap = new HashMap<>();

        for (ProjectFileRelation relation : projectFileRelations) {
            targetMap.put(relation.getTargetFile(), relation);
            for (File child : relation.getRelationFilePaths()) {
                reverseMap.computeIfAbsent(child, k -> new ArrayList<>()).add(relation.getTargetFile());
            }
        }

        Set<File> allTargets = targetMap.keySet();
        Set<File> referenced = reverseMap.keySet();
        Set<File> roots = new HashSet<>(allTargets);
        roots.removeAll(referenced);

        // 对每个 root 节点递归合并其 relationFilePaths
        List<ProjectFileRelation> result = new ArrayList<>();
        Set<File> visitedTargets = new HashSet<>();

        for (File root : roots) {
            ProjectFileRelation rootRelation = new ProjectFileRelation();
            rootRelation.setTargetFile(root);
            List<File> merged = new ArrayList<>();
            Set<File> visitedFiles = new HashSet<>();

            collectDownwardRelations(root, targetMap, visitedFiles, merged);

            rootRelation.setRelationFilePaths(merged);
            result.add(rootRelation);
            visitedTargets.add(root);
        }

        return result;
    }

    @Override
    public List<FileImport> findFileImport() {
        //获取src目录
        File src = new File(srcFileFolder.getAbsolutePath());
        //获取src下面的所有的代码文件
        List<File> allCodeFile = new ArrayList<>();
        getAllCodeFile(src, allCodeFile);

        List<FileImport> fileImports = new ArrayList<>();
        for (File file : allCodeFile) {
            FileImport fileImport = new FileImport();

            JSXImportVisitor visitor = new JSXImportVisitor();
            JSXParse jsxParse = new JSXParse(file.getAbsolutePath());
            FileNode parse = jsxParse.parse(true);
            visitor.visit(parse);
            List<JSXImportVisitor.ImportRecord> imports = visitor.getImports();
            List<FileImportDetail> collect = imports.stream().map(importRecord -> {
                FileImportDetail fileImportDetail = new FileImportDetail();
                fileImportDetail.setImportPath(importRecord.sourcePath);
                fileImportDetail.setImportItems(importRecord.importedNames);
                return fileImportDetail;
            }).collect(Collectors.toList());

            fileImport.setFile(file);
            fileImport.setImports(collect);

            fileImports.add(fileImport);
        }
        return fileImports;
    }

    @Override
    public String getPackageJsonString() {
        File packageJsonFile = new File(
                projectFileFolder.getAbsolutePath() + File.separator + "package.json"
        );

        try {
            return new String(Files.readAllBytes(packageJsonFile.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("getPackageJsonString error", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<PageRouterDefine> findPageRouterDefine() {
        //校验是否引入了react-router
        validReactRouter();
        //获取入口的index.js 或 jsx 、 tsx文件
        //TODO 目前只支持从入口文件开始解析路由
        return null;
    }

    @Override
    public List<PageRouterDefine> parseRouteDefines(String relativeFilePath) {
        if (relativeFilePath == null || relativeFilePath.trim().isEmpty()) {
            throw new ReactProjectValidException("路由配置文件路径不能为空");
        }

        File routeFile = new File(projectFileFolder, relativeFilePath);
        if (!routeFile.exists() || !routeFile.isFile()) {
            throw new ReactProjectValidException("路由配置文件不存在: " + relativeFilePath);
        }

        JSXParse jsxParse = new JSXParse(routeFile.getAbsolutePath());
        FileNode fileNode = jsxParse.parse();
        if (fileNode == null || fileNode.getProgram() == null) {
            return new ArrayList<>();
        }

        ProgramNode programNode = fileNode.getProgram();
        Map<String, Node> bindings = collectTopLevelBindings(programNode);
        Map<String, File> importComponentMap = collectImportComponentMap(programNode, routeFile);
        List<PageRouterDefine> defines = new ArrayList<>();

        for (Node node : programNode.getBody()) {
            if (node instanceof ExportDefaultDeclaration) {
                Node declaration = ((ExportDefaultDeclaration) node).getDeclaration();
                defines.addAll(extractRouteDefinesFromDeclaration(declaration, bindings, importComponentMap, routeFile));
            }
        }

        return defines;
    }

    /**
     * 校验项目中是否引入了react-router
     * @author 卫志龙
     * @date 2025/9/13 09:54
     */
    private void validReactRouter() {
        String packageJsonString = getPackageJsonString();
        JSONObject jsonObject = JSON.parseObject(packageJsonString);
        Object dependencies = jsonObject.get("dependencies");
        if (null == dependencies) {
            throw new ReactProjectValidException("package.json中没有dependencies字段");
        }

        JSONObject dependenciesJson = (JSONObject) dependencies;
        if (!dependenciesJson.containsKey("react-router")
                && !dependenciesJson.containsKey("react-router-dom")) {
            throw new ReactProjectValidException("项目中没有引入react-router或react-router-dom");
        }
    }

    /**
     * 获取所有的代码文件
     * @param folder 当前处理的文件或文件夹
     * @param allCodeFile 收集代码文件的列表
     */
    private void getAllCodeFile(File folder, List<File> allCodeFile) {
        if (Objects.isNull(folder)) {
            return ;
        }
        if (folder.isFile()) {
            Set<Language> languages = projectConfiguration.getLanguages();
            if (languages.contains(Language.JS) && folder.getName().endsWith(".jsx")) {
                allCodeFile.add(folder);
            }
            if (languages.contains(Language.TS) && folder.getName().endsWith(".tsx")) {
                allCodeFile.add(folder);
            }
        }
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (Objects.nonNull(files)) {
                for (File file : files) {
                    getAllCodeFile(file, allCodeFile);
                }
            }
        }
    }

    private Map<String, Node> collectTopLevelBindings(ProgramNode programNode) {
        Map<String, Node> bindings = new HashMap<>();
        if (programNode == null || programNode.getBody() == null) {
            return bindings;
        }
        for (Node node : programNode.getBody()) {
            if (node instanceof VariableDeclarationNode) {
                VariableDeclarationNode declarationNode = (VariableDeclarationNode) node;
                if (declarationNode.getDeclarations() == null) {
                    continue;
                }
                for (VariableDeclarator declarator : declarationNode.getDeclarations()) {
                    if (declarator == null) {
                        continue;
                    }
                    Identifier identifier = declarator.getId();
                    Node init = declarator.getInit();
                    if (identifier != null && init != null) {
                        bindings.put(identifier.getName(), init);
                    }
                }
            }
        }
        return bindings;
    }

    private List<PageRouterDefine> extractRouteDefinesFromDeclaration(Node declaration,
                                                                      Map<String, Node> bindings,
                                                                      Map<String, File> importComponentMap,
                                                                      File routeFile) {
        List<PageRouterDefine> result = new ArrayList<>();
        if (declaration == null) {
            return result;
        }
        if (declaration instanceof ArrayExpression) {
            result.addAll(extractRouteDefinesFromArray((ArrayExpression) declaration, importComponentMap, routeFile));
            return result;
        }
        if (declaration instanceof Identifier) {
            Identifier identifier = (Identifier) declaration;
            Node resolved = bindings.get(identifier.getName());
            if (resolved != null && resolved != declaration) {
                result.addAll(extractRouteDefinesFromDeclaration(resolved, bindings, importComponentMap, routeFile));
            }
        }
        return result;
    }

    private List<PageRouterDefine> extractRouteDefinesFromArray(ArrayExpression arrayExpression,
                                                                Map<String, File> importComponentMap,
                                                                File routeFile) {
        List<PageRouterDefine> defines = new ArrayList<>();
        if (arrayExpression == null || arrayExpression.getElements() == null) {
            return defines;
        }
        for (Expression element : arrayExpression.getElements()) {
            if (element instanceof ObjectExpression) {
                PageRouterDefine define = buildRouteDefinition((ObjectExpression) element, importComponentMap, routeFile);
                if (define != null) {
                    defines.add(define);
                }
            }
        }
        return defines;
    }

    private PageRouterDefine buildRouteDefinition(ObjectExpression objectExpression,
                                                  Map<String, File> importComponentMap,
                                                  File routeFile) {
        if (objectExpression == null || objectExpression.getProperties() == null) {
            return null;
        }

        String path = null;
        String title = null;
        File componentFile = null;

        for (ObjectProperty property : objectExpression.getProperties()) {
            if (property == null) {
                continue;
            }
            String propertyName = resolvePropertyName(property);
            if (propertyName == null) {
                continue;
            }
            Node value = property.getValue();
            switch (propertyName) {
                case "path":
                    path = stringLiteralValue(value);
                    break;
                case "title":
                    title = stringLiteralValue(value);
                    break;
                case "lazy":
                    componentFile = resolveLazyComponentFile(value, routeFile);
                    break;
                case "component":
                    if (componentFile == null) {
                        componentFile = resolveComponentIdentifier(value, importComponentMap);
                    }
                    break;
                default:
                    break;
            }
        }

        if (path == null) {
            return null;
        }

        PageRouterDefine define = new PageRouterDefine();
        define.setRoutePath(path);
        define.setTitle(title != null ? title : "");
        define.setComponentFile(componentFile);
        define.setRelativeFilePath(componentFile != null ? projectRelativePath(componentFile) : null);
        define.setComponentFileExists(componentFile != null && componentFile.exists());
        return define;
    }

    private File resolveComponentIdentifier(Node node, Map<String, File> importComponentMap) {
        if (!(node instanceof Identifier) || importComponentMap == null || importComponentMap.isEmpty()) {
            return null;
        }
        Identifier identifier = (Identifier) node;
        return importComponentMap.get(identifier.getName());
    }

    private String resolvePropertyName(ObjectProperty property) {
        if (property.isComputed()) {
            return null;
        }
        Node key = property.getKey();
        if (key instanceof Identifier) {
            return ((Identifier) key).getName();
        }
        if (key instanceof StringLiteral) {
            return stripQuotes(((StringLiteral) key).getValue());
        }
        return null;
    }

    private String stringLiteralValue(Node node) {
        if (node instanceof StringLiteral) {
            return stripQuotes(((StringLiteral) node).getValue());
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
            if ((first == '\'' && last == '\'')
                    || (first == '"' && last == '"')
                    || (first == '`' && last == '`')) {
                return trimmed.substring(1, trimmed.length() - 1);
            }
        }
        return trimmed;
    }

    private File resolveLazyComponentFile(Node node, File routeFile) {
        if (node instanceof ArrowFunctionExpression) {
            Node body = ((ArrowFunctionExpression) node).getBody();
            File resolved = resolveImportCall(body, routeFile);
            if (resolved != null) {
                return resolved;
            }
        }
        return resolveImportCall(node, routeFile);
    }

    private File resolveImportCall(Node node, File routeFile) {
        if (!(node instanceof CallExpression)) {
            return null;
        }
        CallExpression callExpression = (CallExpression) node;
        if (!(callExpression.getCallee() instanceof ImportExpression)) {
            return null;
        }
        List<Expression> arguments = callExpression.getArguments();
        if (arguments == null || arguments.isEmpty()) {
            return null;
        }
        Expression firstArgument = arguments.get(0);
        if (firstArgument instanceof StringLiteral) {
            String importPath = stripQuotes(((StringLiteral) firstArgument).getValue());
            return resolveComponentFile(routeFile, importPath);
        }
        return null;
    }

    private File resolveComponentFile(File routeFile, String importPath) {
        if (importPath == null || importPath.isEmpty()) {
            return null;
        }

        File base;
        if (importPath.startsWith("./") || importPath.startsWith("../")) {
            File parent = routeFile.getParentFile();
            base = parent == null ? new File(importPath) : new File(parent, importPath);
        } else if (importPath.startsWith("/")) {
            base = new File(projectFileFolder, importPath.substring(1));
        } else if (importPath.startsWith("src/")) {
            base = new File(projectFileFolder, importPath);
        } else if (importPath.startsWith("@/")) {
            base = new File(srcFileFolder, importPath.substring(2));
        } else if (importPath.startsWith("$src/")) {
            base = new File(srcFileFolder, importPath.substring(5));
        } else if (importPath.startsWith("@")) {
            String trimmed = importPath.substring(1);
            if (trimmed.startsWith("/")) {
                trimmed = trimmed.substring(1);
            }
            base = new File(srcFileFolder, trimmed);
        } else {
            base = new File(srcFileFolder, importPath);
        }

        base = base.getAbsoluteFile();
        List<String> extensions = candidateExtensions();
        File resolved = resolveExistingComponent(base, extensions);
        if (resolved != null) {
            return resolved;
        }

        if (hasKnownExtension(importPath, extensions)) {
            return base;
        }

        File parent = base.getParentFile();
        if (parent != null) {
            for (String extension : extensions) {
                File withExtension = new File(parent, base.getName() + extension);
                resolved = resolveExistingComponent(withExtension, extensions);
                if (resolved != null) {
                    return resolved;
                }
            }
        }

        for (String extension : extensions) {
            File indexCandidate = new File(base, "index" + extension);
            resolved = resolveExistingComponent(indexCandidate, extensions);
            if (resolved != null) {
                return resolved;
            }
        }

        if (!extensions.isEmpty()) {
            return new File(base, "index" + extensions.get(0));
        }

        return base;
    }

    private File resolveExistingComponent(File candidate, List<String> extensions) {
        if (candidate.exists()) {
            if (candidate.isFile()) {
                return candidate;
            }
            if (candidate.isDirectory()) {
                File index = resolveIndexFile(candidate, extensions);
                if (index != null) {
                    return index;
                }
            }
        }
        return null;
    }

    private File resolveIndexFile(File directory, List<String> extensions) {
        for (String extension : extensions) {
            File index = new File(directory, "index" + extension);
            if (index.exists()) {
                return index;
            }
        }
        return null;
    }

    private boolean hasKnownExtension(String importPath, List<String> extensions) {
        for (String extension : extensions) {
            if (importPath.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    private List<String> candidateExtensions() {
        LinkedHashSet<String> extensions = new LinkedHashSet<>();
        Set<Language> languages = projectConfiguration.getLanguages();
        if (languages.contains(Language.TS)) {
            extensions.add(".tsx");
            extensions.add(".ts");
        }
        if (languages.contains(Language.JS)) {
            extensions.add(".jsx");
            extensions.add(".js");
        }
        extensions.add(".mjs");
        extensions.add(".cjs");
        return new ArrayList<>(extensions);
    }

    private String projectRelativePath(File file) {
        if (file == null) {
            return null;
        }
        Path projectRoot = projectFileFolder.toPath();
        Path target = file.toPath();
        try {
            if (file.exists()) {
                projectRoot = projectRoot.toRealPath();
                target = target.toRealPath();
            }
            if (target.startsWith(projectRoot)) {
                return projectRoot.relativize(target).toString().replace(File.separatorChar, '/');
            }
        } catch (IOException | IllegalArgumentException e) {
            // ignore and fall back to direct relative path expression
        }
        return target.toString().replace(File.separatorChar, '/');
    }

    private Map<String, File> collectImportComponentMap(ProgramNode programNode, File routeFile) {
        Map<String, File> components = new HashMap<>();
        if (programNode == null || programNode.getBody() == null) {
            return components;
        }
        for (Node node : programNode.getBody()) {
            if (!(node instanceof ImportDeclarationNode)) {
                continue;
            }
            ImportDeclarationNode importDeclarationNode = (ImportDeclarationNode) node;
            if (importDeclarationNode.getSpecifiers() == null || importDeclarationNode.getSpecifiers().isEmpty()) {
                continue;
            }
            String importPath = importDeclarationNode.getSource() == null
                    ? null
                    : stripQuotes(importDeclarationNode.getSource().getValue());
            if (importPath == null) {
                continue;
            }
            File resolved = resolveComponentFile(routeFile, importPath);
            for (Specifier specifier : importDeclarationNode.getSpecifiers()) {
                if (!(specifier instanceof ImportSpecifier)) {
                    continue;
                }
                ImportSpecifier importSpecifier = (ImportSpecifier) specifier;
                Identifier local = importSpecifier.getLocal();
                Identifier imported = importSpecifier.getImported();
                String localName = local != null ? local.getName()
                        : (imported != null ? imported.getName() : null);
                if (localName != null && !localName.isEmpty()) {
                    components.put(localName, resolved);
                }
            }
        }
        return components;
    }

    private void collectDownwardRelations(File current, Map<File, ProjectFileRelation> targetMap,
                                          Set<File> visited, List<File> result) {
        ProjectFileRelation relation = targetMap.get(current);
        if (relation == null) return;

        for (File child : relation.getRelationFilePaths()) {
            if (visited.add(child)) {
                result.add(child);
                collectDownwardRelations(child, targetMap, visited, result);
            }
        }
    }

    private List<FileRelationDetail> srcScan() {
        File file = (this.scanPath == null)
                ? new File(this.srcFileFolder.getPath())
                : new File(this.scanPath.getPath());

        List<FileRelationDetail> details = new ArrayList<>();
        this._projectFileRelation(file, details);
        return details;
    }


    private void _projectFileRelation(File file, List<FileRelationDetail> projectFileRelations) {
        if (file.isDirectory()) {
            for (File childFile : Objects.requireNonNull(file.listFiles())) {
                _projectFileRelation(childFile, projectFileRelations);
            }
        } else if (file.isFile()){
            FileRelationDetail projectFileRelation = new FileRelationDetail();
            Map<String, List<String>> importMap = new HashMap<>();
            projectFileRelation.setTargetFile(file);
            List<File> relationFiles = new ArrayList<>();
            if (file.getPath().endsWith(".jsx") || file.getPath().endsWith(".tsx")) {
                JSXImportVisitor jsxImportVisitor = new JSXImportVisitor();
                JSXParse jsxParse = new JSXParse(file.getAbsolutePath());
                FileNode astNode = jsxParse.parse(true);
                jsxImportVisitor.visit(astNode);
                List<JSXImportVisitor.ImportRecord> importRecords = jsxImportVisitor.getImports();

                for (JSXImportVisitor.ImportRecord importInfo : importRecords) {
                    //目标文件
                    String source = importInfo.sourcePath;
                    //目标文件所导入的组件
                    List<String> importItems = importInfo.importedNames;
                    importMap.put(source, importItems);
                    boolean projectImport = isProjectImport(source);
                    if (projectImport) {
                        //查看引入的文件(source)是否有对应的文件
                        File relativeFile = findFileBySource(source, file);
                        //如果有对应的文件，则添加到关系中
                        if (null != relativeFile) {
                            relationFiles.add(relativeFile);
                        }
                    }
                }
            }
            projectFileRelation.setImportMap(importMap);
            projectFileRelation.setRelationFilePaths(relationFiles);
            projectFileRelations.add(projectFileRelation);
        }
    }

    /**
     * 寻找一条导入语句中，导入的文件在项目中的位置
     * @param source    在 import { refresh } from 'react' 中的'react'部分
     * @param curFile   当前的文件的file对象
     */
    private File findFileBySource(String source, File curFile) {
        File relativeFile;
        Set<Language> languages = projectConfiguration.getLanguages();
        //导入有四种规则
        // 1、../components/CommonCard
        // 2、../components/CommonCard/index.jsx
        // 3、../components/CommonCard/index
        // 4、../components/CommonCard.js
        if (source.startsWith("@") || (source.startsWith("$") && source.contains("src"))) {
            String[] split = source.split("/");
            File currentFile = srcFileFolder;
            for (int i = 1 ; i < split.length; i++) {
                String folder = split[i];

                if (i == split.length - 1) {
                    //当导入规则为4时
                    File currentFile4 = languageStrategy.createNewChildWithPrefix(currentFile, folder);
                    if (null != currentFile4) {
                        return currentFile4;
                    }
                    //当导入规则为2时，直接返回
                    if (folder.contains(".")) {
                        relativeFile = new File(currentFile, folder);
                        return relativeFile;
                    }
                    //当第3种情况时
                    File currentFile1 = languageStrategy.createNewChildWithPrefix(currentFile, folder);
                    if (null != currentFile1) {
                        return currentFile1;
                    }

                    //第1种可能
                    currentFile = new File(currentFile, folder);
                    currentFile1 = languageStrategy.createNewChildIndexFile(currentFile);
                    if (null != currentFile1) {
                        return currentFile1;
                    }
                } else {
                    currentFile = new File(currentFile, folder);
                }
            }

            return languageStrategy.createNewChildIndexFile(currentFile);
        } else if (source.startsWith("../")) {
            File parentFile = curFile;
            parentFile = parentFile.getParentFile();
            String[] split = source.split("/");

            for (int i = 0 ; i < split.length ; i++) {
                String str = split[i];

                if (str.equals("..")) {
                    parentFile = parentFile.getParentFile();
                    continue;
                }

                if (i == split.length - 1) {
                    //导入规则是4时
                    File currentFile4 = languageStrategy.createNewChildWithPrefix(parentFile, str);
                    if (null != currentFile4) {
                        return currentFile4;
                    }
                    //当导入规则为2时，直接返回
                    if (str.contains(".")) {
                        relativeFile = new File(parentFile, str);
                        return relativeFile;
                    }

                    //当第3种情况时
                    File currentFile = languageStrategy.createNewChildIndexFile(parentFile);
                    if (null != currentFile) {
                        return currentFile;
                    }

                    //第1种可能
                    parentFile = new File(parentFile, str);
                    currentFile = languageStrategy.createNewChildIndexFile(parentFile);
                    if (null != currentFile) {
                        return currentFile;
                    }
                } else {
                    parentFile = new File(parentFile, str);
                }
            }
        } else if (source.startsWith("./")) {
            File parentFile = curFile.getParentFile();
            String[] split = source.split("/");
            for (int i = 1 ; i < split.length ; i++) {
                parentFile = new File(parentFile, split[i]);
            }
            if (parentFile.isFile()) {
                return parentFile;
            }
            if (languages.contains(Language.TS)) {
                //直接加.ts或者.tsx
                relativeFile = new File(parentFile.getAbsolutePath() + ".tsx");
                if (relativeFile.isFile()) {
                    return relativeFile;
                }
                relativeFile = new File(parentFile.getAbsolutePath() + ".ts");
                if (relativeFile.isFile()) {
                    return relativeFile;
                }
                //加index.ts或者index.tsx
                relativeFile = new File(parentFile, "index.tsx");
                if (relativeFile.isFile()) {
                    return relativeFile;
                }
                relativeFile = new File(parentFile, "index.ts");
                if (relativeFile.isFile()) {
                    return relativeFile;
                }
            }
            if (languages.contains(Language.JS)) {
                //直接加.js或者.jsx
                relativeFile = new File(parentFile.getAbsolutePath() + ".jsx");
                if (relativeFile.isFile()) {
                    return relativeFile;
                }
                relativeFile = new File(parentFile.getAbsolutePath() + ".js");
                if (relativeFile.isFile()) {
                    return relativeFile;
                }
                //加index.js或者index.jsx
                relativeFile = new File(parentFile, "index.jsx");
                if (relativeFile.isFile()) {
                    return relativeFile;
                }
                relativeFile = new File(parentFile, "index.js");
                if (relativeFile.isFile()) {
                    return relativeFile;
                }
            }
        }
        return null;
    }

    /**
     * 判断一条import语句中， 是否为项目文件
     * @param importPath    导入 的模块路径
     */
    private boolean isProjectImport(String importPath) {
        //如果是@开头的，有可能导入node_modules中的文件，也有可能导入项目中的那文件
        if (importPath.startsWith("@") || importPath.startsWith("$src")) {
            String[] split = importPath.split("/");
            split[0] = split[0].substring(1); // 去掉@符号
            return dfsFileHasProject(srcFileFolder, 1, split);
        }
        return importPath.startsWith("./")
                || importPath.startsWith("../")
                || importPath.startsWith("/")
                || importPath.startsWith("src/")
                || importPath.endsWith(".css")
                || importPath.endsWith(".less")
                || importPath.endsWith(".scss");
    }

    /**
     * 通过dfs方式寻找是否在根目录下，是否存在路径符合split路径的文件。
     * eg: file: 是一个项目的根目录
     * folders: ["src", "components", "CommonCard"]
     * 那么他会寻找 这个项目根目录开始，是否存在 /src/components/CommonCard/ 这个路径。
     * @param file          文件寻找的根目录
     * @param index         folders的下标
     * @param folders       导入的路径
     * @return              是否存在符合路径的文件
     */
    private boolean dfsFileHasProject(File file, int index, String[] folders) {
        if (index >= folders.length) {
            return true;
        }

        File[] children = file.listFiles();
        if (children == null) {
            return false;
        }

        if (index == folders.length - 1) {
            for (File child : children) {
                if (child.isFile()) {
                    String fileName = child.getName().split("\\.")[0];
                    if (fileName.equals(folders[index])) {
                        return true;
                    }
                } else if (child.getName().equals(folders[index])) {
                    file = new File(file, folders[index]);
                    File newChildIndexFile = languageStrategy.createNewChildIndexFile(file);
                    if (null == newChildIndexFile) {
                        continue;
                    }
                    return newChildIndexFile.isFile();
                }
            }
        } else {
            for (File child : children) {
                if (child.isDirectory() && child.getName().equals(folders[index])) {
                    return dfsFileHasProject(child, index + 1, folders);
                }
            }
        }

        return false;
    }



}
