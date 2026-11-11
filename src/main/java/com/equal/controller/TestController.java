package com.equal.controller;

import com.adrninistrator.jacg.conf.ConfigureWrapper;
import com.adrninistrator.jacg.conf.enums.ConfigDbKeyEnum;
import com.adrninistrator.jacg.conf.enums.ConfigKeyEnum;
import com.adrninistrator.jacg.conf.enums.OtherConfigFileUseListEnum;
import com.adrninistrator.jacg.conf.enums.OtherConfigFileUseSetEnum;
import com.adrninistrator.jacg.dto.annotation.*;
import com.adrninistrator.jacg.dto.methodcall.MethodCallLineData4Ee;
import com.adrninistrator.jacg.dto.writedb.WriteDbData4ClassAnnotation;
import com.adrninistrator.jacg.handler.entrymethodinfo.AbstractEntryMethodInfoFiller;
import com.adrninistrator.jacg.handler.entrymethodinfo.EntryMethodInfoFiller4Spring;
import com.adrninistrator.jacg.jardiff.dto.method.ModifiedMethodInfo;
import com.adrninistrator.jacg.jardiff.dto.result.JarDiffResult;
import com.adrninistrator.jacg.jardiff.filter.ModifiedMethodFilterInterface;
import com.adrninistrator.jacg.jardiff.runner.RunnerGenJarDiffCalleeGraph;
import com.adrninistrator.jacg.runner.RunnerGenAllGraph4Callee;
import com.adrninistrator.jacg.runner.RunnerWriteDb;
import com.adrninistrator.jacg.util.JACGJsonUtil;
import com.adrninistrator.javacg2.conf.JavaCG2ConfigureWrapper;
import com.adrninistrator.javacg2.conf.enums.JavaCG2OtherConfigFileUseListEnum;
import com.equal.conifg.BaseConfigureWrapper;
import com.equal.domain.EntryAnnotationInfo;
import com.equal.filter.ModifiedMethodFilter4CalleeGraph;
import com.equal.services.QueryH2Db;
import com.equal.utils.ToolUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.acl.LastOwnerException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/test")
public class TestController extends BaseConfigureWrapper {

    @Autowired
    private QueryH2Db queryH2Db;

    @GetMapping("diff")
    public void diffJar(@RequestParam String appName){
        String outputPathName = appName+"_"+System.currentTimeMillis();

        //获取变化的方法集合
        ModifiedMethodFilter4CalleeGraph modifiedMethodFilter4CalleeGraph = new ModifiedMethodFilter4CalleeGraph();
        Set<String> modifiedMethodInfoSets = doJarDiffCalleeGraph(appName,outputPathName,false, false, modifiedMethodFilter4CalleeGraph);
        //向上查询出入口方法
        Map<String, List<MethodCallLineData4Ee>> entryMapReturnInMemory = getEntryMapReturnInMemory(appName,modifiedMethodInfoSets);


        List<WriteDbData4ClassAnnotation> writeDbData4ClassAnnotations = queryH2Db.queryClassAnnotation(appName);
        Map<String, List<WriteDbData4ClassAnnotation>> classAnnotationMap = writeDbData4ClassAnnotations.stream().collect(Collectors.groupingBy(WriteDbData4ClassAnnotation::getClassName));


        //获取所有入口方法
        Map<String,EntryAnnotationInfo> entryAnnotationInfoMap = getEntryMethod(entryMapReturnInMemory,classAnnotationMap);

    }



    @GetMapping("genAllGraph4CalleeToFile")
    public String genAllGraph4CalleeToFile(){
        ConfigureWrapper configureWrapper = genConfigureWrapper();
        configureWrapper.setMainConfig(ConfigKeyEnum.CKE_APP_NAME, "dictionary"+"_2");
        configureWrapper.setMainConfig(ConfigKeyEnum.CKE_CALL_GRAPH_WRITE_TO_FILE, Boolean.TRUE.toString());
        RunnerGenAllGraph4Callee runnerGenAllGraph4Callee = new RunnerGenAllGraph4Callee(configureWrapper);
        runnerGenAllGraph4Callee.run();
        return "aa";
    }

    @GetMapping("genAllGraphToDb")
    public String genAllGraphToDb(@RequestParam String appName,@RequestParam String jarVersion){
        ConfigureWrapper configureWrapper = genConfigureWrapper();
        JavaCG2ConfigureWrapper javaCG2ConfigureWrapper = genJavaCG2ConfigureWrapper();

        configureWrapper.setMainConfig(ConfigKeyEnum.CKE_APP_NAME, appName+"_"+jarVersion);
        javaCG2ConfigureWrapper.setOtherConfigList(JavaCG2OtherConfigFileUseListEnum.OCFULE_JAR_DIR, "./libs/"+jarVersion+"/dtp-electricity-server.jar");
        RunnerWriteDb runnerWriteDb = new RunnerWriteDb(javaCG2ConfigureWrapper, configureWrapper);
        runnerWriteDb.preHandle();
        runnerWriteDb.createTables(true);
        runnerWriteDb.run();
        return "ok";
    }

    @GetMapping("queryDiffResult")
    public String queryH2db(@RequestParam String appName){
        ConfigureWrapper configureWrapper = genConfigureWrapper();
        configureWrapper.setMainConfig(ConfigKeyEnum.CKE_APP_NAME, appName);
        return "ok";
    }



    /**
     * 查询出变化的方法
     * @param useFiler4Spring
     * @param useFiler4CommandXml
     * @param modifiedMethodFilters
     * @return
     */
    public static Set<String>  doJarDiffCalleeGraph(String appName,String outputPathName,boolean useFiler4Spring, boolean useFiler4CommandXml, ModifiedMethodFilterInterface... modifiedMethodFilters) {
        ConfigureWrapper configureWrapper = genConfigureWrapper();
        JavaCG2ConfigureWrapper javaCG2ConfigureWrapper = genJavaCG2ConfigureWrapper();
        String JAR_GZ_DIR = "./libs/";
        String JAR_DIR_NAME_OLD_VERSION1 = appName+"/old";
        String JAR_DIR_NAME_NEW_VERSION2 = appName+"/new";
        configureWrapper.setMainConfig(ConfigKeyEnum.CKE_APP_NAME, appName);
        configureWrapper.setMainConfig(ConfigKeyEnum.CKE_OUTPUT_ROOT_PATH, "");
        configureWrapper.setMainConfig(ConfigKeyEnum.CKE_OUTPUT_DIR_NAME, outputPathName);

        configureWrapper.setOtherConfigList(OtherConfigFileUseListEnum.OCFULE_JAR_DIFF_CALLEE_GRAPH_DIR, JAR_GZ_DIR + JAR_DIR_NAME_OLD_VERSION1, JAR_GZ_DIR + JAR_DIR_NAME_NEW_VERSION2);

        try (EntryMethodInfoFiller4Spring entryMethodInfoFiller4Spring = new EntryMethodInfoFiller4Spring(configureWrapper)) {
            List<AbstractEntryMethodInfoFiller> fillerList = new ArrayList<>();
            if (useFiler4Spring) {
                fillerList.add(entryMethodInfoFiller4Spring);
            }
            if (useFiler4CommandXml) {
                configureWrapper.addOtherConfigList(OtherConfigFileUseListEnum.OCFULE_EXTENSIONS_CODE_PARSER, "");
            }
            AbstractEntryMethodInfoFiller[] fillers = fillerList.toArray(new AbstractEntryMethodInfoFiller[]{});

            RunnerGenJarDiffCalleeGraph genJarDiffCalleeGraph = new RunnerGenJarDiffCalleeGraph(javaCG2ConfigureWrapper, configureWrapper, fillers, modifiedMethodFilters);
            genJarDiffCalleeGraph.setSkipWriteDb(false);
            JarDiffResult jarDiffResult = genJarDiffCalleeGraph.generate();
            Map<String, List<ModifiedMethodInfo>> jarModifiedMethodInfoMap = jarDiffResult.getJarModifiedMethodInfoMap();
            ToolUtil.printMapContent(jarModifiedMethodInfoMap);
            Set<String> modifiedMethodInfos = new HashSet<>();
            for (String s : jarModifiedMethodInfoMap.keySet()) {
                for (ModifiedMethodInfo modifiedMethodInfo : jarModifiedMethodInfoMap.get(s)) {
                    modifiedMethodInfos.add(modifiedMethodInfo.getFullMethod());
                }
            }
            return modifiedMethodInfos;
        }
    }

    /**
     * 查询出变化方法向上影响的入口方法堆栈，
     * @param sets
     * @return
     */
    public static Map<String, List<MethodCallLineData4Ee>> getEntryMapReturnInMemory(String appName,Set<String> sets) {
        ConfigureWrapper configureWrapper = genConfigureWrapper();
        configureWrapper.setMainConfig(ConfigKeyEnum.CKE_APP_NAME, appName);
        configureWrapper.setMainConfig(ConfigDbKeyEnum.CDKE_DB_TABLE_SUFFIX, "_2");

        configureWrapper.setMainConfig(ConfigKeyEnum.CKE_CALL_GRAPH_WRITE_TO_FILE, Boolean.FALSE.toString());
        configureWrapper.setMainConfig(ConfigKeyEnum.CKE_CALL_GRAPH_RETURN_IN_MEMORY, Boolean.TRUE.toString());
        configureWrapper.setOtherConfigSet(OtherConfigFileUseSetEnum.OCFUSE_METHOD_CLASS_4CALLEE,sets);

        RunnerGenAllGraph4Callee runnerGenAllGraph4Callee = new RunnerGenAllGraph4Callee(configureWrapper);
        runnerGenAllGraph4Callee.run();
        return runnerGenAllGraph4Callee.getAllMethodCallLineData4EeMap();
    }

    /**
     * 获取所有得入口方法
     * @return
     */
    public static Map<String,EntryAnnotationInfo>  getEntryMethod(Map<String, List<MethodCallLineData4Ee>> allMethodCallLineData4EeMap,Map<String, List<WriteDbData4ClassAnnotation>> classAnnotationMap) {
        Map<String,EntryAnnotationInfo> entryAnnotationInfoMap = new HashMap<>();
        //解析并只返回入口方法
        for (String s : allMethodCallLineData4EeMap.keySet()) {
            for (MethodCallLineData4Ee methodCallLineData4Ee : allMethodCallLineData4EeMap.get(s)) {
                if (methodCallLineData4Ee.isEntryMethod()){
                    genEntryMethod(methodCallLineData4Ee,classAnnotationMap,entryAnnotationInfoMap);
                }
            }
        }

        return entryAnnotationInfoMap;
    }

    /**
     * 对每一个入口方法进行处理，获取方法与对应类上得注解信息
     * @param methodCallLineData4Ee
     * @param classAnnotationMap
     */
    public static void genEntryMethod(MethodCallLineData4Ee methodCallLineData4Ee,Map<String, List<WriteDbData4ClassAnnotation>> classAnnotationMap,Map<String,EntryAnnotationInfo> entryAnnotationInfoMap){

        String actualFullMethod = methodCallLineData4Ee.getActualFullMethod();
        String fullClassName = actualFullMethod.split(":")[0];
        String methodName = actualFullMethod.split(":")[1];
        //方法上的注解
        Map<String, Map<String, BaseAnnotationAttribute>> methodAnnotationMapSingle = methodCallLineData4Ee.getMethodAnnotationMap();
        //类上的注解
        List<WriteDbData4ClassAnnotation> writeDbData4ClassAnnotations = classAnnotationMap.get(fullClassName);
        if (writeDbData4ClassAnnotations!=null){
            if (entryAnnotationInfoMap.containsKey(fullClassName)){
                EntryAnnotationInfo entryAnnotationInfo = entryAnnotationInfoMap.get(fullClassName);
                Map<String, Map<String, Map<String, BaseAnnotationAttribute>>> methodAnnotationMap = entryAnnotationInfo.getMethodAnnotationMap();
                methodAnnotationMap.put(actualFullMethod,methodCallLineData4Ee.getMethodAnnotationMap());
                entryAnnotationInfo.setMethodAnnotationMap(methodAnnotationMap);
            }else {
                EntryAnnotationInfo entryAnnotationInfo1 = new EntryAnnotationInfo();
                Map<String, Map<String, Map<String, BaseAnnotationAttribute>>> tmpMap = new HashMap<>();
                entryAnnotationInfo1.setClassAnnotationList(writeDbData4ClassAnnotations);
                tmpMap.put(actualFullMethod,methodAnnotationMapSingle);
                entryAnnotationInfo1.setMethodAnnotationMap(tmpMap);
                entryAnnotationInfoMap.put(fullClassName,entryAnnotationInfo1);
            }
        }
    }



    public static void main(String[] args) {
        String appName = "dic";
        rundiff(appName);

    }

    public static void rundiff(String appName){
        String outputPathName = appName+System.currentTimeMillis();

        //获取变化的方法集合
        ModifiedMethodFilter4CalleeGraph modifiedMethodFilter4CalleeGraph = new ModifiedMethodFilter4CalleeGraph();
        Set<String> modifiedMethodInfoSets = doJarDiffCalleeGraph(appName,outputPathName,false, false, modifiedMethodFilter4CalleeGraph);
        //向上查询出入口方法
        Map<String, List<MethodCallLineData4Ee>> entryMapReturnInMemory = getEntryMapReturnInMemory(appName,modifiedMethodInfoSets);


        QueryH2Db queryH2Db1 = new QueryH2Db();
        List<WriteDbData4ClassAnnotation> writeDbData4ClassAnnotations = queryH2Db1.queryClassAnnotation(appName);
        Map<String, List<WriteDbData4ClassAnnotation>> classAnnotationMap = writeDbData4ClassAnnotations.stream().collect(Collectors.groupingBy(WriteDbData4ClassAnnotation::getClassName));

        //获取所有入口方法
        Map<String,EntryAnnotationInfo> entryAnnotationInfoMap = getEntryMethod(entryMapReturnInMemory,classAnnotationMap);
        ToolUtil.printMapContent(entryAnnotationInfoMap);
        log.info("以下为受到影响的接口");
        showApi(outputPathName);

    }
    public static void showApi(String outputPathName){
        ArrayList<String> files = new ArrayList<>();
        File[] fileList = new File("./_jacg_o_ee/"+outputPathName+"/").listFiles();
        if (fileList != null) {
            for (File file : fileList) {
                if (file.isFile()) {
                    if (file.getName().contains(".txt")){
                        files.add("./_jacg_o_ee/"+outputPathName+"/"+file.getName());
                    }
                }
            }
        }

        Set<String> set = new HashSet<>();
        for (String file : files) {
            readFile(file,set);
        }
        for (String s : set) {
            log.info(s);
        }
    }
    public static void readFile(String filePath,Set<String> set){
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {

                if (line.contains("!entry!")){
                    /**目前只找接口，后续可能还有mq，定时任务等，再具体找**/
                    //接口
                    boolean b1 = line.contains("org.springframework.web.bind.annotation");
                    //定时任务
                    boolean b2 = line.contains("org.springframework.scheduling.annotation.Scheduled");
                    boolean b3 = line.contains("toString():java.lang.String");
                    boolean b4 = line.contains("com.yushu");
                    boolean b5 = line.contains("com.persagy");
                    if (b1){
                        String s = line.split("Mapping")[1].split("\\s+")[0];
                        if (s.equals("Get")){
                            log.info(filePath);
                            log.info(line);
                        }
                        set.add(s);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
