package com.siemens.cto.aem.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {

    @RequestMapping(value = "/about")
    public String about() {
        return "aem/about";
    }

    @RequestMapping(value = "/")
    public String index() {
        return "aem/index";
    }

    @RequestMapping(value = "/sandbox")
    public String sandbox() {
        return "aem/sandbox";
    }

}
