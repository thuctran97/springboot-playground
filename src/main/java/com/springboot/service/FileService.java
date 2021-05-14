/**
 * Copyright (c) 2021 Absolute Software Corporation. All rights reserved. Reproduction or
 * transmission in whole or in part, in any form or by any means, electronic, mechanical or
 * otherwise, is prohibited without the prior written consent of the copyright owner.
 */
package com.springboot.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

@Service
public class FileService {
    public static String existingBucketName = "dv2corp1-ap-scheduler-svc";
    public TransferManager tm;
    public AmazonS3 s3Client;

    public FileService(){
        s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.DEFAULT_REGION)
                .withCredentials(new ProfileCredentialsProvider())
                .build();
        tm = TransferManagerBuilder.standard()
                .withS3Client(s3Client)
                .build();
    }

    public void writeContent(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
    }

    public String getFileName(HttpServletRequest request){
        MultipartResolver resolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        MultipartHttpServletRequest multipartRequest = resolver.resolveMultipart(request);
        CommonsMultipartFile multipartFile =  null;
        Iterator<String> iterator = multipartRequest.getFileNames();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            multipartFile = (CommonsMultipartFile) multipartRequest.getFile(key);
        }
        return multipartFile.getOriginalFilename();
    }

    public void upload(InputStream inputStream, String fileName, ObjectMetadata metadata){
        System.out.println("--------upload-----------");
        PutObjectRequest request = new PutObjectRequest(
                existingBucketName, fileName, inputStream, metadata);
        request.setGeneralProgressListener(progressEvent -> System.out.println("Transferred bytes: " +
                progressEvent.getBytesTransferred()));
        Upload upload = tm.upload(request);
        System.out.println("Object upload started");
        try {
            upload.waitForCompletion();
            System.out.println("Object upload complete");
        } catch (AmazonClientException | InterruptedException amazonClientException) {
            System.out.println("Unable to upload file, upload aborted.");
            amazonClientException.printStackTrace();
        }
    }
}
