package com.neo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neo.Utils.MimeTypes;
import com.neo.domain.Report;
import com.neo.domain.Template;
import com.neo.service.ReportService;
import com.neo.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author Berg
 */
@Slf4j
@Controller
public class DownloadController {

    @Autowired
    TemplateService templateService;

    @Autowired
    ReportService reportService;

    /**
     * 下载模板
     *
     * @param id 根据id下载模板
     * @return 模板文件
     */
    @RequestMapping("/downloadTemplate")
    public ResponseEntity<byte[]> downloadTemplate(@RequestParam("id") String id) {
        log.debug("下载文件:id={}", id);
        Template template = templateService.getById(id);
        String defaultExt = MimeTypes.getDefaultExt(template.getType());
        byte[] bytes = template.getData();
        String fileName = template.getName() + '.' + defaultExt;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", template.getType() + ";charset=utf-8");
        headers.setContentDispositionFormData("attachment",
                new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
        headers.setContentLength(bytes.length);
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    /**
     * 根据模板id和学生id下载实验报告 !Deprecated
     *
     * @param templateId 实验报告id
     * @param studentId  学生id
     * @return 实验报告文件
     */
    @RequestMapping("/downloadReportDeprecated")
    public ResponseEntity<byte[]> downloadReport(@RequestParam("templateId") String templateId,
                                                 @RequestParam("studentId") String studentId) {
        Report report = getReportByTemplateIdAndStudentId(templateId, studentId);
        return getResponseEntity(report);
    }


    /**
     * 根据实验报告id下载实验报告
     *
     * @param reportId id
     * @return 实验报告文件
     */
    @RequestMapping("/downloadReport")
    public ResponseEntity<byte[]> downloadReport(@RequestParam("reportId") String reportId) {
        log.debug("下载文件:id={}", reportId);
        Report report = getReportById(reportId);
        return getResponseEntity(report);
    }

    /**
     * 根据id返回实验报告实体
     *
     * @param rid report id
     * @return Report实体
     */
    private Report getReportById(String rid) {
        return reportService.getById(rid);
    }

    /**
     * 根据模板id和学生id返回实验报告实体
     *
     * @param templateId 实验报告id
     * @param studentId  学生id
     * @return Report实体
     */
    private Report getReportByTemplateIdAndStudentId(String templateId, String studentId) {
        QueryWrapper<Report> reportQueryWrapper = new QueryWrapper<>();
        reportQueryWrapper.eq("uploader", studentId)
                .eq("report_template", templateId);
        return reportService.getOne(reportQueryWrapper);
    }

    /**
     * 工具方法,获取根据Report实体返回ResponseEntity
     *
     * @param report Report实体
     * @return ResponseEntity
     */
    private ResponseEntity<byte[]> getResponseEntity(Report report) {
        byte[] bytes = report.getData();
        String defaultExt = MimeTypes.getDefaultExt(report.getType());
        String fileName = String.format("%s.%s", report.getFilename(), defaultExt);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", report.getType());
        headers.setContentDispositionFormData("attachment",
                new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
        headers.setContentLength(bytes.length);
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    /**
     * 返回pdf,浏览器内联显示(iframe/embed)
     *
     * @param rid 实验报告id
     * @return 内联Report响应
     */
    @GetMapping(value = "/inlineDisplay")
    public ResponseEntity<InputStreamResource> getTermsConditions(@RequestParam("rid") String rid) {
        QueryWrapper<Report> reportQueryWrapper = new QueryWrapper<>();
        reportQueryWrapper.eq("rid", rid);
        Report report = reportService.getOne(reportQueryWrapper);
        byte[] bytes = report.getData();
        HttpHeaders headers = new HttpHeaders();
        headers.add("content-disposition", "inline;filename=" + report.getFilename());
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(bytes));
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(bytes.length)
                .contentType(MediaType.parseMediaType("application/pdf"))
                .body(resource);
    }


    @GetMapping(value = "/getNoReport")
    @ResponseBody
    public String getNoReport() {
        return "<h1>没有找到提交的实验报告。</h1>";
    }
}


