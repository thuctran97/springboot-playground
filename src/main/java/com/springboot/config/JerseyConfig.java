/**
 * Copyright (c) 2021 Absolute Software Corporation. All rights reserved. Reproduction or
 * transmission in whole or in part, in any form or by any means, electronic, mechanical or
 * otherwise, is prohibited without the prior written consent of the copyright owner.
 */
package com.springboot.config;

import com.springboot.controller.FileUploadController;

//import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import javax.ws.rs.ApplicationPath;

@Component
public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        register(FileUploadController.class);
//        register(MultiPartFeature.class);
    }
}
