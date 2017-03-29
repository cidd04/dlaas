package com.equinix.dlaas.domain;

/**
 * Created by ransay on 3/23/2017.
 */
public class FileUploadMessage {

    private String fileName;

    private FileUploadType type;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public FileUploadType getType() {
        return type;
    }

    public void setType(FileUploadType type) {
        this.type = type;
    }
}
