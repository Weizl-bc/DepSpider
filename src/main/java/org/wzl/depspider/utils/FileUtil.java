package org.wzl.depspider.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * File工具类
 *
 * @author weizhilong
 */
public class FileUtil {

    /**
     * 读取文件内容
     * @param filePath      文件路径
     * @return              文件内容
     * @throws IOException  异常
     */
    public static String getInputString(String filePath) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            bufferedReader.lines().forEach(e -> stringBuilder.append(e).append("\n"));
        }
        return stringBuilder.toString();
    }

    public static String getInputString(File file) throws IOException {
        return getInputString(file.getAbsolutePath());
    }

    /**
     * 读取文件内容
     * @param file 文件
     * @return 文件内容
     * @throws IOException 异常
     */
    public static String readFileContent(File file) throws IOException {
        return getInputString(file);
    }


    /**
     * 从指定的根文件夹开始，按照给定的路径层级依次进入子目录，返回最终的目标文件夹。
     *
     * @param rootFile     根目录（起始文件夹）
     * @param pathSegments 子目录路径段，每个元素表示一级子目录
     * @return 最终定位到的目标文件夹
     */
    public static File resolvePath(File rootFile, List<String> pathSegments) {
        File file = rootFile;
        for (String pathSegment : pathSegments) {
            file = new File(file, pathSegment);
            if (!file.isDirectory()) {
                throw new RuntimeException("文件不存在");
            }
        }
        return file;
    }

    /**
     * 校验文件和文件后缀名
     * @param filePath      文件路径
     * @param fileSuffix    文件后缀名
     */
    public static void validateFile(String filePath, String fileSuffix) {
        if (null == filePath || filePath.isEmpty()) {
            throw new RuntimeException("文件路径不能为空");
        }
        if (null == fileSuffix || fileSuffix.isEmpty()) {
            throw new RuntimeException("文件后缀名不能为空");
        }

        if (!filePath.endsWith(fileSuffix)) {
            throw new RuntimeException("文件后缀名不匹配，期望后缀名为：" + fileSuffix);
        }
        if (!new java.io.File(filePath).exists()) {
            throw new RuntimeException("文件不存在");
        }
        if (!new java.io.File(filePath).isFile()) {
            throw new RuntimeException("文件不存在");
        }
    }

}
