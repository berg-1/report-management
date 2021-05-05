package com.neo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neo.domain.Report;
import com.neo.exception.LargeFileException;
import com.neo.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Date;
import java.util.UUID;

/**
 * @author Berg
 */
@Controller
public class ReportController {

    @Autowired
    ReportService reportService;

    /**
     * 最大上传限制
     */
    private static final Long MAX_UPLOAD_SIZE = 2 * 10 * 1024 * 1024L;

    @PostMapping("/uploadReport")
    public String singleFileUpload(@RequestParam(value = "file") MultipartFile file,
                                   @RequestParam(value = "studentId", defaultValue = "undefined") String studentId,
                                   @RequestParam(value = "templateId", defaultValue = "undefined") String templateId,
                                   RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("messageId", templateId);
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "请选择文件再上传!");
            return "redirect:mainStudent";
        }
        try {
            // 取得文件并以Bytes方式保存
            byte[] bytes = file.getBytes();
            if (bytes.length > MAX_UPLOAD_SIZE) {
                throw new LargeFileException(MAX_UPLOAD_SIZE);
            }
            String uuid = UUID.randomUUID().toString();
            reportService.save(new Report(uuid,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    studentId,
                    templateId,
                    new Date(),
                    bytes));
            redirectAttributes.addFlashAttribute("success",
                    "文件'" + file.getOriginalFilename() + "'上传成功!");
        } catch (LargeFileException largeFileException) {
            redirectAttributes.addFlashAttribute("message", String.format(
                    "文件过大,上传失败!请将文件控制在%dMB内!",
                    largeFileException.getMaxSize() / 1048576
            ));
        } catch (Exception s) {
            redirectAttributes.addFlashAttribute("message", "上传失败!");
            s.printStackTrace();
        }
        return "redirect:mainStudent";
    }

    @GetMapping("/deleteReport")
    String deleteTemplate(@RequestParam("templateId") String templateId,
                          @RequestParam("studentId") String studentId) {
        QueryWrapper<Report> reportQueryWrapper = new QueryWrapper<>();
        reportQueryWrapper.eq("report_template", templateId);
        QueryWrapper<Report> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("report_template", templateId)
                .eq("uploader", studentId);
        reportService.remove(queryWrapper);
        return "redirect:mainStudent";
    }


}
