package com.equal.services;


import com.adrninistrator.jacg.common.DC;
import com.adrninistrator.jacg.common.enums.DbTableInfoEnum;
import com.adrninistrator.jacg.conf.ConfigureWrapper;
import com.adrninistrator.jacg.conf.enums.ConfigDbKeyEnum;
import com.adrninistrator.jacg.conf.enums.ConfigKeyEnum;
import com.adrninistrator.jacg.dboper.DbInitializer;
import com.adrninistrator.jacg.dboper.DbOperWrapper;
import com.adrninistrator.jacg.dboper.DbOperator;
import com.adrninistrator.jacg.dto.writedb.*;
import com.adrninistrator.jacg.dto.writedb.base.BaseWriteDbData;
import com.equal.conifg.BaseConfigureWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class QueryH2Db extends BaseConfigureWrapper {
    private HashMap<String,Class> map;

    public static void main(String[] args) {
        String jarVersion = "old";
        QueryH2Db queryH2Db = new QueryH2Db();
        queryH2Db.getAllInfo("electricity","old");
    }
    public List<WriteDbData4ClassAnnotation> queryClassAnnotation(String appName){
        //查下类上的所有注解信息
        ConfigureWrapper configureWrapper = genConfigureWrapper();
        configureWrapper.setMainConfig(ConfigKeyEnum.CKE_APP_NAME, appName);
        configureWrapper.setMainConfig(ConfigDbKeyEnum.CDKE_DB_TABLE_SUFFIX, "_2");

        DbOperWrapper dbOperWrapper = DbInitializer.genDbOperWrapper(configureWrapper, this);
        DbOperator dbOperator = dbOperWrapper.getDbOperator();
        String sql = "select * from " + DbTableInfoEnum.DTIE_CLASS_ANNOTATION.getTableName();
        List<WriteDbData4ClassAnnotation> writeDbData4ClassAnnotations = dbOperator.queryList(dbOperWrapper.formatSql(sql), WriteDbData4ClassAnnotation.class);
        try {
            dbOperator.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return writeDbData4ClassAnnotations;
    }

    public List<WriteDbData4MethodAnnotation> queryAnnotationResult(ConfigureWrapper configureWrapper){
        DbOperWrapper dbOperWrapper = DbInitializer.genDbOperWrapper(configureWrapper, this);
        DbOperator dbOperator = dbOperWrapper.getDbOperator();
        String sql = "select * from " + DbTableInfoEnum.DTIE_METHOD_ANNOTATION.getTableName();
        List<WriteDbData4MethodAnnotation> writeDbData4MethodAnnotations = dbOperator.queryList(dbOperWrapper.formatSql(sql), WriteDbData4MethodAnnotation.class);
        try {
            dbOperator.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return  writeDbData4MethodAnnotations;
    }

    public void getAllInfo(String appName,String jarVersion){
        ConfigureWrapper configureWrapper = genConfigureWrapper();

        configureWrapper.setMainConfig(ConfigKeyEnum.CKE_APP_NAME, appName+"_"+jarVersion);

        DbOperWrapper dbOperWrapper = DbInitializer.genDbOperWrapper(configureWrapper, this);
        DbOperator dbOperator = dbOperWrapper.getDbOperator();
        DbTableInfoEnum[] values = DbTableInfoEnum.values();
        for (DbTableInfoEnum value : values) {
            Class aClass = map.get(value.getTableNameKeyword());
            if (aClass!=null){
                String sql = "select * from " + value.getTableName();
                List<BaseWriteDbData> jarInfoTmp = dbOperator.queryList(dbOperWrapper.formatSql(sql),aClass);
                if (!jarInfoTmp.isEmpty()){
                    log.info(jarInfoTmp.get(1).toString());
                }
            }
        }
        try {
            dbOperator.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public QueryH2Db(){
        map = new HashMap<>();
        map.put("javacg2_config", WriteDbData4JavaCG2Config.class);
        map.put("class_annotation",WriteDbData4ClassAnnotation.class);
        map.put("class_info", WriteDbData4ClassInfo.class);
        map.put("class_name", WriteDbData4ClassName.class);
        map.put("class_reference", WriteDbData4ClassReference.class);
        map.put("class_signature_generics_type", WriteDbData4ClassSignatureGenericsType.class);
        map.put("class_ext_impl_generics_type", WriteDbData4ClassExtImplGenericsType.class);
        map.put("extends_impl", WriteDbData4ExtendsImpl.class);
        map.put("enum_init_arg_field", WriteDbData4EnumInitArgField.class);
        map.put("enum_init_assign_info", WriteDbData4EnumInitAssignInfo.class);
        map.put("inner_class", WriteDbData4InnerClass.class);
        map.put("jar_info", WriteDbData4JarInfo.class);
        map.put("lambda_method_info", WriteDbData4LambdaMethodInfo.class);
        map.put("method_annotation", WriteDbData4MethodAnnotation.class);
        map.put("method_arg_annotation", WriteDbData4MethodArgAnnotation.class);
        map.put("method_arg_generics_type", WriteDbData4MethodArgGenericsType.class);
        map.put("method_argument", WriteDbData4MethodArgument.class);
        map.put("method_call", WriteDbData4MethodCall.class);
        map.put("method_call_info", WriteDbData4MethodCallInfo.class);
        map.put("method_call_static_field", WriteDbData4MethodCallStaticField.class);
        map.put("method_info", WriteDbData4MethodInfo.class);
        map.put("method_line_number", WriteDbData4MethodLineNumber.class);
        map.put("method_return_generics_type", WriteDbData4MethodReturnGenericsType.class);
        map.put("mybatis_ms_table", WriteDbData4MyBatisMSTable.class);
        map.put("mybatis_ms_write_table", WriteDbData4MyBatisMSWriteTable.class);
        map.put("sf_field_method_call", WriteDbData4SfFieldMethodCall.class);
        map.put("spring_bean", WriteDbData4SpringBean.class);
        map.put("spring_controller", WriteDbData4SpringController.class);
        map.put("spring_task", WriteDbData4SpringTask.class);
        map.put("field_annotation", WriteDbData4FieldAnnotation.class);
        map.put("field_info", WriteDbData4FieldInfo.class);
        map.put("get_method", WriteDbData4GetMethod.class);
        map.put("set_method", WriteDbData4SetMethod.class);
        map.put("set_method_assign_info", WriteDbData4SetMethodAssignInfo.class);
        map.put("field_relationship", WriteDbData4FieldRelationship.class);
        map.put("mybatis_ms_column", WriteDbData4MybatisMSColumn.class);
        map.put("mybatis_ms_entity", WriteDbData4MybatisMSEntity.class);
        map.put("field_generics_type", WriteDbData4FieldGenericsType.class);
        map.put("properties_conf", WriteDbData4PropertiesConf.class);
        map.put("mybatis_ms_set_column", WriteDbData4MyBatisMSSetColumn.class);
        map.put("mybatis_ms_where_column", WriteDbData4MyBatisMSWhereColumn.class);
        map.put("mybatis_ms_select_column", WriteDbData4MyBatisMSSelectColumn.class);
        map.put("method_return_arg_seq", WriteDbData4MethodReturnArgSeq.class);
        map.put("method_return_call_id", WriteDbData4MethodReturnCallId.class);
        map.put("method_return_const_value", WriteDbData4MethodReturnConstValue.class);
        map.put("method_return_field_info", WriteDbData4MethodReturnFieldInfo.class);
        map.put("method_catch", WriteDbData4MethodCatch.class);
        map.put("method_finally", WriteDbData4MethodFinally.class);
        map.put("method_throw", WriteDbData4MethodThrow.class);
        map.put("mybatis_ms_get_set_db", WriteDbData4MybatisMSGetSetDb.class);
        map.put("method_call_method_call_return", WriteDbData4MethodCallMethodCallReturn.class);
        map.put("mybatis_ms_formated_sql", WriteDbData4MyBatisMSFormatedSql.class);
    }

}
