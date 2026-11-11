package com.equal.filter;

import com.adrninistrator.jacg.jardiff.dto.method.ModifiedMethodInfo;
import com.adrninistrator.jacg.jardiff.filter.ModifiedMethodFilterInterface;

import java.util.Arrays;
import java.util.List;

public class ModifiedMethodFilter4CalleeGraph implements ModifiedMethodFilterInterface {
    @Override
    public boolean skipMethod(ModifiedMethodInfo modifiedMethodInfo) {
        // 假如发生变化的方法不是Controller则跳过
        String fullMethod = modifiedMethodInfo.getFullMethod();
        return !(fullMethod.contains("com.yushu.")||fullMethod.contains("com.persagy."));
    }
}
