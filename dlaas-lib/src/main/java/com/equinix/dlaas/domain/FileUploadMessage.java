package com.equinix.dlaas.domain;

import java.io.Serializable;

/**
 * Created by ransay on 3/23/2017.
 */
public class FileUploadMessage implements Serializable {

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
