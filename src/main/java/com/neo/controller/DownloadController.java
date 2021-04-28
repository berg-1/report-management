package com.neo.controller;

import com.neo.domain.Template;
import com.neo.mapper.TemplateMapper;
import com.neo.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Berg
 */
@Controller
@Slf4j
public class DownloadController {

    @Autowired
    TemplateService templateService;

    @RequestMapping("/downloadTemplate")
    public ResponseEntity<byte[]> downloadTemplate(@RequestParam("id") String id) {
        log.info("下载文件:id={}", id);
        Template template = templateService.getById(id);
        byte[] bytes = template.getData();
        String fileName = template.getName();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", template.getType());
        headers.setContentDispositionFormData("attachment", fileName);
        headers.setContentLength(bytes.length);

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

}
