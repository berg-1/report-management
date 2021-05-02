package com.neo.controller;

import com.neo.domain.Report;
import com.neo.exception.LargeFileException;
import com.neo.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
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
    private static final Long MAX_UPLOAD_SIZE = 20971520L;

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
            redirectAttributes.addFlashAttribute("message",
                    "文件'" + file.getOriginalFilename() + "'上传成功!");

        } catch (LargeFileException largeFileException) {
            redirectAttributes.addFlashAttribute("message", String.format(
                    "文件过大,上传失败!请将文件控制在%dMB内!",
                    largeFileException.getMaxSize() / 1048576
            ));
        } catch (IOException s) {
            redirectAttributes.addFlashAttribute("message", "上传失败!");
            s.printStackTrace();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.toString());
            e.printStackTrace();
        }
        return "redirect:mainStudent";
    }

}
