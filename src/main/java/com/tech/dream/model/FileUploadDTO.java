package com.tech.dream.model;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FileUploadDTO {

    MultipartFile file;
    String fileUrl;
    String fileType;
    String uploadType;

    public FileUploadDTO(MultipartFile file,String fileUrl, String fileType, String uploadType) {
        this.file = file;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.uploadType = uploadType;
    }
}