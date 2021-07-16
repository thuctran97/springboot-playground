/**
 * Copyright (c) 2021 Absolute Software Corporation. All rights reserved. Reproduction or
 * transmission in whole or in part, in any form or by any means, electronic, mechanical or
 * otherwise, is prohibited without the prior written consent of the copyright owner.
 */
package com.springboot.controller;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.springboot.service.FileService;

//import org.glassfish.jersey.media.multipart.FormDataParam;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import jdk.internal.util.xml.impl.Input;

@RestController
public class FileUploadController {
    @Autowired
    FileService fileService;

    @RequestMapping(value = "/upload", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String handleUpload(InputStream inputStream) throws IOException {
        Date date = new Date();
        ObjectMetadata metadata = new ObjectMetadata();
        fileService.uploadViaFile(inputStream, "file"+ date.getTime(), metadata);
        return "success";
    }

}
