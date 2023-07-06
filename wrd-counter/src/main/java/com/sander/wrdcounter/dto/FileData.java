package com.sander.wrdcounter.dto;

import jakarta.persistence.Id;

public class FileData {
    private String id;
    private String fileContent;

    public FileData(String id, String fileContent) {
        this.id = id;
        this.fileContent = fileContent;
    }
}
