/**
 * Copyright (c) 2021 Absolute Software Corporation. All rights reserved. Reproduction or
 * transmission in whole or in part, in any form or by any means, electronic, mechanical or
 * otherwise, is prohibited without the prior written consent of the copyright owner.
 */
package com.springboot.controller;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.springboot.service.FileService;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

@RestController
public class FileUploadController {
    @Autowired
    FileService fileService;

    public final int MAX_MERMORY_sIZE = 1024 * 1024 * 3;
    public final int MAX_FILE_sIZE = 1024 * 1024 * 500;
    public final int MAX_REQUEST_SIZE = 1024 * 1024 * 1000;

    @RequestMapping(value = "/normal-upload", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> uploadFileTest(@RequestParam("file") MultipartFile file) throws IOException {
        File convertFile = new File("C:\\Users\\ttran\\Downloads\\temp\\" + file.getOriginalFilename());
        convertFile.createNewFile();
        FileOutputStream fout = new FileOutputStream(convertFile);
        fout.write(file.getBytes());
        fout.close();
        return new ResponseEntity<>("File uploaded successfully", HttpStatus.OK);
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String handleUpload(HttpServletRequest request) {
//        String fileName = fileService.getFileName(request);
//        System.out.println("fileName: "+fileName);
        ServletFileUpload upload = new ServletFileUpload();
        try {
            FileItemIterator iterStream =  upload.getItemIterator(request);
            int count = 0;
            while (iterStream.hasNext()) {
                System.out.println("count: " + count++);
                FileItemStream item = iterStream.next();
                String name = item.getFieldName();
                InputStream stream = item.openStream();
                if (!item.isFormField()) {
                    fileService.upload(stream, "temp", new ObjectMetadata());
                    stream.close();
                } else {
                    String formFieldValue = Streams.asString(stream);
                    System.out.println("formFieldValue: "+formFieldValue);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "success";
    }

    @RequestMapping(value = "/upload2", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String handleUpload2(HttpServletRequest request) throws FileUploadException, IOException {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        System.out.println("isMultipart: "+ isMultipart);
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setRepository(
                new File(System.getProperty("java.io.tmpdir")));
        factory.setSizeThreshold(MAX_MERMORY_sIZE);
        factory.setFileCleaningTracker(null);
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(MAX_FILE_sIZE);
        upload.setSizeMax(MAX_REQUEST_SIZE);
        List items = upload.parseRequest(request);
        System.out.println("list items: "+ items.size());
        Iterator iter = items.iterator();
        int count = 0;
        while (iter.hasNext()) {
            FileItem item = (FileItem) iter.next();
            System.out.println("count: " + count++);
            if (!item.isFormField()) {
                try (
                        InputStream uploadedStream = item.getInputStream();
                        OutputStream out = new FileOutputStream("file.mov");) {

                    IOUtils.copy(uploadedStream, out);
                }
            }
        }
        return "success!";
    }

}
