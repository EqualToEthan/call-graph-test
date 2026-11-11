package com.equal.domain;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class TypeInfo {
    @ExcelProperty("class_code")
    private String classCode;
    @ExcelProperty("code")
    private String code;
    @ExcelProperty("name")
    private String name;
    @ExcelProperty("data_source")
    private String dataSource;
}
