/**
 * Copyright (c) 2021 Absolute Software Corporation. All rights reserved. Reproduction or
 * transmission in whole or in part, in any form or by any means, electronic, mechanical or
 * otherwise, is prohibited without the prior written consent of the copyright owner.
 */
package com.springboot.controller;

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
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

@RestController
public class FileUploadController {
    @Autowired
    FileService fileService;

    @RequestMapping(value = "/upload", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String handleUpload(HttpServletRequest request) {
//        Long bodyLength = request.getContentLengthLong();
//        System.out.println("body length: " + bodyLength);
        ObjectMetadata metadata = new ObjectMetadata();
        try {
            String fileSize = request.getHeader("FileSize");
            System.out.println("file size : " + fileSize);
            metadata.setContentLength(new Long(fileSize));
        } catch (Exception e) {
            System.out.println("File size is not provided");
        }

        ServletFileUpload upload = new ServletFileUpload();
        Date date = new Date();
        try {
            FileItemIterator iterStream =  upload.getItemIterator(request);
            while (iterStream.hasNext()) {
                FileItemStream item = iterStream.next();
                InputStream inputStream = item.openStream();
                if (!item.isFormField()) {
                    fileService.uploadViaFile(inputStream, "file"+ date.getTime(), metadata);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "success";
    }

}
