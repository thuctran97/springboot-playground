/**
 * Copyright (c) 2021 Absolute Software Corporation. All rights reserved. Reproduction or
 * transmission in whole or in part, in any form or by any means, electronic, mechanical or
 * otherwise, is prohibited without the prior written consent of the copyright owner.
 */
package com.springboot.controller;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.springboot.service.FileService;

//import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/files")
public class FileUploadController {
    @Autowired
    FileService fileService;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/upload")
    public String handleUpload(InputStream inputStream) throws IOException {
        Date date = new Date();
        ObjectMetadata metadata = new ObjectMetadata();
        fileService.uploadViaInputstream(inputStream, "file"+ date.getTime(), metadata);
        return "success";
    }

}
