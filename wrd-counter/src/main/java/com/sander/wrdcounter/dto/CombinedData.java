package com.sander.wrdcounter.dto;

import org.springframework.web.multipart.MultipartFile;

public class CombinedData {
    public MultipartFile file;
    public ProcessingFlags processingFlags;

    public CombinedData(MultipartFile file, ProcessingFlags processingFlags) {
        this.file = file;
        this.processingFlags = processingFlags;
    }
}
