package com.equal.utils;

import com.adrninistrator.jacg.util.JACGJsonUtil;
import com.adrninistrator.javacg2.util.JavaCG2Util;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public class ToolUtil {
    public static  <T> void printMapContent(Map<String, T> map, String... flags) {

        if (JavaCG2Util.isMapEmpty(map)) {
            log.info("map为空");
            return;
        }
        List<String> keyList = new ArrayList<>(map.keySet());
        Collections.sort(keyList);
        for (String key : keyList) {
            T value = map.get(key);
            log.info("key: " + key);
            log.info("value: " + JACGJsonUtil.getJsonStrPretty(value));
        }
    }

}
