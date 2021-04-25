package com.neo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Berg
 */
@Controller
public class IndexController {

    /**
     * 来登录页
     *
     * @return 登录页地址
     */
    @GetMapping(value = {"/", "/login"})
    public String loginPage() {
        return "login";
    }

}
