package com.siemens.cto.aem.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by Z003BPEJ on 6/18/14.
 */
@Controller
public class SamlController {

    @RequestMapping(value = "/idp")
    public String idProvider() {
        return "saml/idp";
    }

    @RequestMapping(value = "/post")
    public String idProviderPost() {
        return "saml/post";
    }

}
