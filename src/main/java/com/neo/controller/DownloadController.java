package com.neo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neo.domain.Report;
import com.neo.domain.Student;
import com.neo.domain.Template;
import com.neo.mapper.ReportMapper;
import com.neo.service.ReportService;
import com.neo.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Berg
 */
@Slf4j
@Controller
public class DownloadController {

    @Autowired
    TemplateService templateService;

//    @Autowired
//    ReportService reportService;

    @Autowired
    ReportMapper reportMapper;

    @RequestMapping("/downloadTemplate")
    public ResponseEntity<byte[]> downloadTemplate(@RequestParam("id") String id) {
        log.info("下载文件:id={}", id);
        Template template = templateService.getById(id);
        byte[] bytes = template.getData();
        String fileName = template.getName();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", template.getType() + ";charset=utf-8");
        headers.setContentDispositionFormData("attachment",
                new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
        headers.setContentLength(bytes.length);

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @RequestMapping("/downloadReport")
    public ResponseEntity<byte[]> downloadReport(@RequestParam("templateId") String templateId,
                                                 @RequestParam("studentId") String studentId) {
        Report report = getReportByTemplateIdAndStudentId(templateId, studentId);

        return getResponseEntity(report);
    }

    private ResponseEntity<byte[]> getResponseEntity(Report report) {
        byte[] bytes = report.getData();
        String fileName = report.getFilename();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", report.getType());
        headers.setContentDispositionFormData("attachment",
                new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
        headers.setContentLength(bytes.length);
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @RequestMapping("/downloadReportById")
    public ResponseEntity<byte[]> downloadReport(@RequestParam("reportId") String reportId) {
        log.info("下载文件:id={}", reportId);
        Report report = getReportById(reportId);
        return getResponseEntity(report);
    }


    public Report getReportById(String rid) {
        return reportMapper.selectById(rid);
    }

    public Report getReportByTemplateIdAndStudentId(String templateId, String studentId) {
        QueryWrapper<Report> reportQueryWrapper = new QueryWrapper<>();
        reportQueryWrapper.eq("uploader", studentId)
                .eq("report_template", templateId);
        return reportMapper.selectOne(reportQueryWrapper);
    }

}
