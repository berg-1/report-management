package com.neo.controller;

import com.neo.domain.Template;
import com.neo.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Berg
 */
@Controller
@Slf4j
public class DownloadController {

    @Autowired
    TemplateService templateService;

    @PostMapping("/downloadTemplate")
    public String downloadTemplate(@RequestParam("id") String id) {
        log.info("下载文件:id={}", id);
        Template template = templateService.getById(id);
        byte[] bytes = template.getData();
        String filename = template.getName();
        
        return "redirect:main";
    }

}
