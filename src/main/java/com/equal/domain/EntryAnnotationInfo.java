package com.equal.domain;

import com.adrninistrator.jacg.dto.annotation.BaseAnnotationAttribute;
import com.adrninistrator.jacg.dto.writedb.WriteDbData4ClassAnnotation;
import com.adrninistrator.jacg.util.JACGJsonUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Data
@Slf4j
public class EntryAnnotationInfo {
    private List<WriteDbData4ClassAnnotation> classAnnotationList;
    private Map<String,Map<String, Map<String, BaseAnnotationAttribute>>> methodAnnotationMap;


    public Map<String,String> getMapString(Map<String, Map<String, Map<String, BaseAnnotationAttribute>>> methodAnnotationMap ){
        HashMap<String, String> objectObjectHashMap = new HashMap<>();
        for (String s : methodAnnotationMap.keySet()) {
            Map<String, Map<String, BaseAnnotationAttribute>> stringMapMap = methodAnnotationMap.get(s);
            for (String string : stringMapMap.keySet()) {
                Map<String, BaseAnnotationAttribute> stringBaseAnnotationAttributeMap = stringMapMap.get(string);
                for (String s1 : stringBaseAnnotationAttributeMap.keySet()) {
                    BaseAnnotationAttribute baseAnnotationAttribute = stringBaseAnnotationAttributeMap.get(s1);
                    objectObjectHashMap.put(s+"-"+string+"-"+s1, JACGJsonUtil.getJsonStrPretty(baseAnnotationAttribute));

                }
            }
        }
        return objectObjectHashMap;
    }
}
