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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Service
public class FileService {
    public static String existingBucketName = "s3://dv2corp1-ap-scheduler-svc";
    public static String keyName            = "AKIATS566GDNZV6W5CYA";

    public void writeContent(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
    }

    public void upload(InputStream inputStream){
        System.out.println("++++++++upload+++++++");
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.DEFAULT_REGION)
                .withCredentials(new ProfileCredentialsProvider())
                .build();
        TransferManager tm = TransferManagerBuilder.standard()
                .withS3Client(s3Client)
                .build();
        PutObjectRequest request = new PutObjectRequest(
                existingBucketName, keyName, inputStream, new ObjectMetadata());
        request.setGeneralProgressListener(new ProgressListener() {
            @Override
            public void progressChanged(ProgressEvent progressEvent) {
                System.out.println("Transferred bytes: " +
                        progressEvent.getBytesTransferred());
            }
        });
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
