package org.wzl.depspider.react.project;

import org.wzl.depspider.react.dto.FileImport;
import org.wzl.depspider.react.dto.PageRouterDefine;
import org.wzl.depspider.react.dto.ProjectFileRelation;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface IReactProjectOperator {

    /**
     * 获取项目文件关系
     * 通过一个文件的import来判断
     * @return  项目文件关系列表
     *
     */
    List<ProjectFileRelation> jsxFileRelation();

    /**
     * 查找项目中的哪些文件import了函数、或组件
     * 该函数可以通过 导入名和导入的内容来查找
     * @param importMap     导入的信息
     *                      eg. importMap= { "react", [ "useState" ] }，那么就会寻找项目中引入的react的useState的代码文件
     *                      value值支持传null，如果是null，则发现引入了key，则直接返回该文件
     * @return              符合条件的代码文件
     */
    List<File> findJsxFileWithImport(Map<String, List<String>> importMap);

    /**
     * 获取项目文件关系，通过递归来将所有的子文件也包含进去
     * @param originRelation   源关系列表
     * @return  项目文件关系列表，
     */
    List<ProjectFileRelation> deepSearchProjectRelation(List<ProjectFileRelation> originRelation);

    /**
     * 获取所有文件的import信息
     * @return  项目所有文件的import信息
     */
    List<FileImport> findFileImport();

    /**
     * 获取项目的package.json文件信息，返回的时json字符串
     * @return  package.json文件
     */
    String getPackageJsonString();

    /**
     * 获取项目中的页面路由定义
     * @return 页面路由定义列表
     */
    List<PageRouterDefine> findPageRouterDefine();

    /**
     * 解析指定路由配置文件中的页面路由定义
     * @param relativeFilePath 相对项目根目录的路由配置文件路径（例如 src/routes/bee.js）
     * @return 该文件中声明的页面路由信息
     */
    List<PageRouterDefine> parseRouteDefines(String relativeFilePath);
}
