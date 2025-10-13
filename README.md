# DepSpider
This framework is built with Java and is designed to scan and analyze page-level component dependencies within React-based front-end projects.
这个框架由 Java 构建，旨在扫描和分析基于 React 的前端项目中的页面级组件依赖关系。
---

### 引入依赖：

```xml
<dependency>
    <groupId>io.github.1807149205</groupId>
    <artifactId>depspider</artifactId>
    <version>0.0.9</version>
</dependency>
```

### 用法
准备工作：
```java
import org.wzl.depspider.react.project.ReactProjectOperator;
import org.wzl.depspider.react.project.config.language.Language
import org.wzl.depspider.react.project.config.language.*

import java.util.Set;

//配置
ProjectConfiguration projectConfiguration = new ProjectConfiguration();
//配置项目的语言，有ts和js语言
Set<Language> languages = new HashSet<>();
languages.add(Language.JS);
languages.add(Language.TS);
projectConfiguration.setLanguages(languages);

//配置项目要扫描的路径
List<String> scanPath = new ArrayList<>();
scanPath.add("src");
scanPath.add("pages");
projectConfiguration.setScanPath(scanPath);

//操作类
IReactProjectOperator reactProjectOperator = new ReactProjectOperator(
        "D:\\gitlab\\wd-operation-front",   //前端项目根目录
        projectConfiguration                //配置
);

```
#### 1、获取项目依赖关系
```java
IReactProjectOperator reactProjectOperator = new ReactProjectOperator(
        "D:\\gitlab\\wd-operation-front",   //前端项目根目录
        projectConfiguration                //配置
);
List<ProjectFileRelation> relations = reactProjectOperator.jsxFileRelation();
```
返回示例：
```json
[
  {
    "targetFile": "src/pages/home/index.jsx",
    "relationFilePaths": [
      "src/pages/home/data.js",
      "src/component/List/index.jsx",
    ]
  },
  {
    ...
  }
]
```

#### 2、使用 `JSXObjectVisitor` 获取文件中的对象表达式

`JSXObjectVisitor` 可以帮助你遍历单个 JSX/TSX 文件并收集其中出现的 `ObjectExpression` 节点，便于后续进行分析或转换。最简单的使用方式如下：

```java
String indexPath = "/path/to/your/file.jsx";
JSXParse jsxParse = new JSXParse(indexPath);
FileNode fileNode = jsxParse.parse();

JSXObjectVisitor visitor = new JSXObjectVisitor();
visitor.visit(fileNode);

// 输出采集到的对象表达式及其上下文路径
// ObjectRecord 位于 org.wzl.depspider.ast.jsx.visitor 包中
for (ObjectRecord record : visitor.getObjectRecords()) {
    System.out.println(record.getPath() + " => " + record.getExpression());
}
```

如果你想直接运行一个示例，可以执行仓库中的 `JSXObjectVisitorExample`：

```bash
mvn -DskipTests=true -q compile
java -cp target/classes org.wzl.depspider.example.JSXObjectVisitorExample /path/to/your/file.jsx
```

示例程序会打印出文件中所有的对象表达式及其对应的路径，便于快速验证访问结果。
