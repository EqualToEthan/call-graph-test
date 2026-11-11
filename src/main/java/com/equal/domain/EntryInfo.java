package com.equal.domain;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class EntryInfo {
    private String className;

    private List<String> entryMethodName;
}
