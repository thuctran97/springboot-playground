/**
 * Copyright (c) 2021 Absolute Software Corporation. All rights reserved. Reproduction or
 * transmission in whole or in part, in any form or by any means, electronic, mechanical or
 * otherwise, is prohibited without the prior written consent of the copyright owner.
 */
package com.springboot.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
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

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

@Service
public class FileService {
    public static String existingBucketName = "dv2corp1-ap-scheduler-svc";
    public TransferManager transferManager;
    public AmazonS3 s3Client;
    public DefaultAWSCredentialsProviderChain credentialProviderChain;


    public FileService() {
        s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.DEFAULT_REGION)
                .withCredentials(new ProfileCredentialsProvider())
                .build();
        transferManager = TransferManagerBuilder.standard()
                .withS3Client(s3Client)
                .build();
    }

    private Upload initUploadProcess(File uploadedFile, String key) {
        PutObjectRequest request = new PutObjectRequest(existingBucketName, key, uploadedFile);
        request.setGeneralProgressListener(progressEvent -> System.out.println("Transferred bytes: " +
                progressEvent.getBytesTransferred()));
        return transferManager.upload(request);
    }

    public void uploadViaFile(File uploadedFile, String key) throws IOException {
        Upload upload = initUploadProcess(uploadedFile, key);
        System.out.println("Object upload started");
        try {
            upload.waitForCompletion();
            transferManager.shutdownNow();
            uploadedFile.delete();
            System.out.println("Object upload complete");
        } catch (AmazonClientException | InterruptedException amazonClientException) {
            System.out.println("Unable to upload file, upload aborted.");
            amazonClientException.printStackTrace();
        }
    }
}
