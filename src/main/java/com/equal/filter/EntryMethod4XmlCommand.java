package com.equal.filter;

import com.adrninistrator.jacg.dto.entrymethodinfo.BaseEntryMethodInfo;

public class EntryMethod4XmlCommand extends BaseEntryMethodInfo {
    private String commandKey;

    public EntryMethod4XmlCommand() {
        this.type = TestParser4XmlCommand.DATA_TYPE;
    }

    public String getCommandKey() {
        return commandKey;
    }

    public void setCommandKey(String commandKey) {
        this.commandKey = commandKey;
    }
}
