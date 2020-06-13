package com.tech.dream.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DigitalOceanUtil {

    @Value("${digitalocean.bucketname}")
    private String S3_BUCKET_NAME;

    @Value("${digitalocean.fullendpoint}")
    private String DO_FULL_ENDPOINT;

    @Autowired
    AmazonS3 s3Client;

    public String saveFile(MultipartFile multipartFile, String fileName, String folder,Long folderId, String assetType) throws IOException {
        String key = folder + "/" + folderId + "/"+ assetType + "/" + fileName;
        File fileToUpload = convertFromMultiPartToFile(multipartFile);
        s3Client.putObject(new PutObjectRequest(S3_BUCKET_NAME, key, fileToUpload)
                .withCannedAcl(CannedAccessControlList.PublicRead));

        fileToUpload.delete();
        return DO_FULL_ENDPOINT + "/" + folder + "/" + folderId + "/"+ assetType + "/" + fileName;
    }

    private File convertFromMultiPartToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(multipartFile.getBytes());
        fos.close();
        return file;
    }
}