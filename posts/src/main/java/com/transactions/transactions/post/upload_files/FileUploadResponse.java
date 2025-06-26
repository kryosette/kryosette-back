package com.transactions.transactions.post.upload_files;

import lombok.Data;

@Data
public final class FileUploadResponse {
    private String filename;
    private String uniqueFilename;
    private long fileSize;
    private String fileType;
    private String fileUrl;
}
