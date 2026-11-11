package com.equal.utils;

import org.apache.bcel.generic.PUSH;

public class Constants {
    public static String PROJECT_PATH = System.getProperty("user.dir");
    public static String MAVEN_PATH = PROJECT_PATH +"/maven";
    public static String MAVEN_SETTING_PATH = PROJECT_PATH +"/maven/conf/settings.xml";
    public static String GIT_PATH = PROJECT_PATH +"/git";
    public static final  String MAVEN_ORDER = "clean package -Dmaven.test.skip=true";
    public static String JAVA_HOME = System.getProperty("java.home");
    public static final String HEAD = "HEAD";
    public static final  String POM_PATH = "/pom.xml";
    public static final String TARGET = "/target";
    public static final String JAR = ".jar";
    public static final String ORIGINAL = "original";
    public static final String METHOD_RELATION_SUFFIX = ".txt";
    public static final String ANNOTATION_SUFFIX = "-annotation.txt";
    public static final String METHOD_M = "M:";
    public static final String CLASS_C = "C:";
    public static final String SPLIT_BLANK = "\\s+";
    public static final String MAPPING = "Mapping";
    public static final String SPLIT_M = ":";
}
