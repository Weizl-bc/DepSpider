# DepSpider
This framework is built with Java and is designed to scan and analyze page-level component dependencies within React-based front-end projects.
这个框架由 Java 构建，旨在扫描和分析基于 React 的前端项目中的页面级组件依赖关系。
---

### 引入依赖：

```xml
<dependency>
    <groupId>io.github.1807149205</groupId>
    <artifactId>depspider</artifactId>
    <version>0.0.10</version>
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

#### 3、解析单个路由配置文件

```java
List<PageRouterDefine> routes = reactProjectOperator.parseRouteDefines("src/routes/bee.js");
for (PageRouterDefine route : routes) {
    System.out.println(route.getRoutePath()
            + " => " + route.getRelativeFilePath()
            + " (exists: " + route.isComponentFileExists() + ")");
}
```

如果 `bee.js` 中定义了类似下面的导出：

```javascript
export default [
  {
    path: '/gpsmap',
    lazy: () => import('MicroSiteBee/gpsmap'),
    title: '',
  }
];
```

则输出会包含 `"/gpsmap => src/MicroSiteBee/gpsmap/index.jsx"` 等信息，`relativeFilePath` 会自动适配 `.jsx`、`.tsx` 等常见后缀；当组件文件不存在时，`isComponentFileExists()` 将返回 `false`，便于快速识别异常路由。

对于通过 `component` 属性直接引用已导入组件的写法，同样能够解析出对应的组件文件，例如：

```javascript
import AppTabbar from '@/pages/app-tabbar';

export default [
  {
    path: '/apphome',
    component: AppTabbar,
    title: '网点管家首页',
  },
];
```

这类路由会生成 `"/apphome => src/pages/app-tabbar/index.jsx"` 之类的结果。路径中像 `@/`、`$src/` 的常见别名也会自动映射到 `src` 目录，便于兼容不同项目的路径风格。
