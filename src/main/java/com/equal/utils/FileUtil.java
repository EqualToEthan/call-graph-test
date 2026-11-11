package com.equal.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
public class FileUtil {
    /**
     * 删除目录下所有文件
     *
     * @param dir
     */
    public static void removeDir(File dir) {
        if (null == dir) {
            return;
        }
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                removeDir(file);
            } else {
                System.out.println(file + ":" + file.delete());
            }
        }
    }

    /**
     * 替换maven 本地仓库存储路径
     * @throws IOException
     */
    public static void replaceTxtByStr() throws IOException {
        Path p = Paths.get(Constants.MAVEN_SETTING_PATH);
        List<String> lines = Files.readAllLines(p);
        List<String> lines_New = new ArrayList<>();
        lines.forEach(s -> {
            if (s.contains("<localRepository>")&&s.contains("</localRepository>")){
                String str = "<localRepository>"+ Constants.PROJECT_PATH + "/maven/repository</localRepository>";
                lines_New.add(str);
            }else {
                lines_New.add(s);
            }
        });
        Files.delete(p);
        File file = new File(Constants.MAVEN_SETTING_PATH);
        Path p2 = Paths.get(file.getAbsolutePath());
        Files.write(p2,lines_New);
    }

    /**
     * 获取指定目录jar文件全路径
     * @param gitDir
     * @return
     */
    public static String getJarPath(String gitDir){
        File file = new File(gitDir);
        List<File> collect = Arrays.stream(file.listFiles()).filter(file1 -> {
            if (file1.getName().contains(Constants.JAR) && !file1.getName().contains(Constants.ORIGINAL)) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());
        if (collect.size()==1){
            return gitDir+File.separator+collect.get(0).getName();
        }
        return null;
    }
}
