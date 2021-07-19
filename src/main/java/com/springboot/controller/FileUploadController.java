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

    public void validateHash(File inputFile, String fileHash) throws IOException{
        String hash256;
        InputStream inputStream = new FileInputStream(inputFile);
        try (HashingInputStream hashingInputStream = new HashingInputStream(Hashing.sha256(), inputStream)) {
            while (hashingInputStream.read() != -1) {
            }
            hash256 = hashingInputStream.hash().toString();
        }
        if (!hash256.equals(fileHash)) {
            throw new IOException("Hash mismatch: " + hash256 + "; expected: " + fileHash);
        }
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String handleUpload(HttpServletRequest request) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        ServletFileUpload upload = new ServletFileUpload();
        Date date = new Date();
        File inputFile = null;
        try {
            FileItemIterator iterStream =  upload.getItemIterator(request);
            while (iterStream.hasNext()) {
                FileItemStream item = iterStream.next();
                try (InputStream inputStream = item.openStream()) {
                    if (!item.isFormField()) {
                        if (null == inputFile) {
                            inputFile = new File(item.getName());
                        }
                        OutputStream out = new FileOutputStream(inputFile);
                        IOUtils.copy(inputStream, out);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String fileHash = request.getHeader(FILE_HASH);
        validateHash(inputFile, fileHash);
        fileService.uploadViaFile(inputFile, "file" + date.getTime());
        return "success";
    }

}
