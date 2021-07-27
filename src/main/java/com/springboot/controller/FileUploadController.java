/**
 * Copyright (c) 2021 Absolute Software Corporation. All rights reserved. Reproduction or
 * transmission in whole or in part, in any form or by any means, electronic, mechanical or
 * otherwise, is prohibited without the prior written consent of the copyright owner.
 */
package com.springboot.controller;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;

import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.springboot.service.FileService;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

@RestController
public class FileUploadController {
    public static final String FILE_HASH = "fileHash";

    @Autowired
    FileService fileService;

    @Autowired
    List<String> hashList;

    public void validateHashWithHashInputStream(File uploadedFile, String fileHash) throws IOException{
        System.out.println("Starting to check sha-256 hash - 1");
        String hash256;
        InputStream inputStream = new FileInputStream(uploadedFile);
        try (HashingInputStream hashingInputStream = new HashingInputStream(Hashing.sha256(), inputStream)) {
            while (hashingInputStream.read() != -1) {
            }
            hash256 = hashingInputStream.hash().toString();
        }
        if (!hash256.equalsIgnoreCase(fileHash)) {
            throw new IOException("Hash mismatch: " + hash256 + "; expected: " + fileHash);
        }
        System.out.println("Hash is correct");
    }



    public void validateHashWithMessageDigest(File uploadedFile, String fileHash) throws IOException {
        System.out.println("Starting to check sha-256 hash - 2");
        MessageDigest shaDigest = null;
        InputStream inputStream = new FileInputStream(uploadedFile);
        try {
            shaDigest = MessageDigest.getInstance("SHA-256");
            try (DigestInputStream dis = new DigestInputStream(inputStream, shaDigest)) {
                while (dis.read() != -1) ;
                shaDigest = dis.getMessageDigest();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        StringBuilder result = new StringBuilder();
        for (byte b : shaDigest.digest()) {
            result.append(String.format("%02x", b));
        }
        if (!result.toString().equalsIgnoreCase(fileHash)){
            throw new IOException("Hash mismatch: " + result + "; expected: " + fileHash);
        }
        System.out.println("Hash is correct");
    }

    public void validateHasWithMDLib(File uploadedFile, String fileHash) throws IOException {
        System.out.println("Starting to check md5 hash - 3");
        String hash = com.twmacinta.util.MD5.asHex(com.twmacinta.util.MD5.getHash(uploadedFile));
        if (!hash.equalsIgnoreCase(fileHash)){
            throw new IOException("Hash mismatch: " + hash + "; expected: " + fileHash);
        }
        System.out.println("Hash is correct");
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String handleUpload(HttpServletRequest request) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        ServletFileUpload upload = new ServletFileUpload();
        String fileHash = request.getHeader(FILE_HASH);
        Date date = new Date();
        File inputFile = null;

        MessageDigest shaDigest = null;
        try {
            shaDigest = MessageDigest.getInstance("SHA-256");
            FileItemIterator iterStream =  upload.getItemIterator(request);
            String objectKey = "file"+ date.getTime();

            while (iterStream.hasNext()) {
                FileItemStream item = iterStream.next();
                try (InputStream inputStream = item.openStream()) {
                    if (!item.isFormField()) {
                        DigestInputStream digestInputStream = new DigestInputStream(inputStream, shaDigest);
                        fileService.uploadViaFile(digestInputStream, objectKey);

                        /*if (null == inputFile) {
                            inputFile = new File(item.getName());
                        }
                        OutputStream out = new FileOutputStream(inputFile);
                        IOUtils.copy(inputStream, out);

                         */
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!validateHash(shaDigest, fileHash))
            return "Hash mismatch error";

        return "success";
    }

    private boolean validateHash(MessageDigest shaDigest, String fileHash){
        if (shaDigest == null) {
            System.out.println("shaDigest can't be initialized");
            return false;
        }
        /*
        StringBuilder result = new StringBuilder();
        for (byte b : shaDigest.digest()) {
            result.append(String.format("%02x", b));
        }*

         */
        String calculatedHashString = Hex.encodeHexString(shaDigest.digest());
        System.out.println("calculatedHashString: " + calculatedHashString + ", fileHash: " + fileHash);
        return calculatedHashString.equalsIgnoreCase(fileHash);
    }

}
