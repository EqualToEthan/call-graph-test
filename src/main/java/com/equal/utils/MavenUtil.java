package com.equal.utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.util.Collections;

@Slf4j
public class MavenUtil {
    /**
     * @param mavenPath  maven系统路径 如D:\apache-maven-3.5.4
     * @param pomPath    要操控的pom文件的系统路径 如：D:\coding\**\pom.xml
     * @param mavenOrder maven命令如：clean
     */
    public static void operationMavenOrder(String mavenPath, String pomPath, String mavenOrder) {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setJavaHome(new File(Constants.JAVA_HOME));
        //想要操控的pom文件的位置
        request.setPomFile(new File(pomPath));
        //操控的maven命令
        request.setGoals(Collections.singletonList(mavenOrder));

        Invoker invoker = new DefaultInvoker();
        //maven的位置
        invoker.setMavenHome(new File(mavenPath));
        invoker.setLogger(new PrintStreamLogger(System.err, InvokerLogger.DEBUG) {
        });
        invoker.setOutputHandler(System.out::println);
        try {
            invoker.execute(request);
        } catch (MavenInvocationException e) {
            e.printStackTrace();
            log.info("maven 编译失败");
        }
    }
}
