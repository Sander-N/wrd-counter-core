package com.sander.wrdcounter.dto;

public class MQData {
    private FileData fileData;
    private ProcessingFlags processingFlags;
    private Boolean lastMsg;

    public MQData(FileData fileData, ProcessingFlags processingFlags, Boolean lastMsg) {
        this.fileData = fileData;
        this.processingFlags = processingFlags;
        this.lastMsg = lastMsg;
    }
}
