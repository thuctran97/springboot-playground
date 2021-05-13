/**
 * Copyright (c) 2021 Absolute Software Corporation. All rights reserved. Reproduction or
 * transmission in whole or in part, in any form or by any means, electronic, mechanical or
 * otherwise, is prohibited without the prior written consent of the copyright owner.
 */
package com.springboot.controller;

import com.springboot.service.FileService;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
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

import javax.servlet.http.HttpServletRequest;

@RestController
public class FileUploadController {
    @Autowired
    FileService fileService;

    @RequestMapping(value = "/upload2", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
        System.out.println("---------doPost-----------:" + request);
        ServletFileUpload upload = new ServletFileUpload();
        try {
            FileItemIterator iterStream =  upload.getItemIterator(request);
            while (iterStream.hasNext()) {
                FileItemStream item = iterStream.next();
                String name = item.getFieldName();
                InputStream stream = item.openStream();
                if (!item.isFormField()) {
//                    fileService.writeContent(stream);
                    fileService.upload(stream);
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

}
