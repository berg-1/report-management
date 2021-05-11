package com.neo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neo.domain.Report;
import com.neo.exception.LargeFileException;
import com.neo.service.ReportService;
import com.neo.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ReportController {

    @Autowired
    ReportService reportService;

    @Autowired
    TemplateService templateService;
    /**
     * 最大上传限制
     */
    private static final Long MAX_UPLOAD_SIZE = 2 * 10 * 1024 * 1024L;

    /**
     * 上传一个Report实体到数据库
     *
     * @param file               文件数据 -> MultipartFile
     * @param studentId          上传者id
     * @param templateId         模板id
     * @param redirectAttributes 重定向传值需要
     * @return mian_student.html
     */
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
            Date uploadDate = new Date();
            reportService.save(new Report(uuid,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    studentId,
                    templateId,
                    uploadDate,
                    bytes,
                    uploadDate.before(getTemplateDeadlineById(templateId))));
            if (uploadDate.after(getDeadLineByTemplateId(templateId))) {
                log.info("学生提交时间超出截至日期!id={},templateId={}", studentId, templateId);
                redirectAttributes.addFlashAttribute("message", "上传成功,但已过截至日期!");
            } else {
                redirectAttributes.addFlashAttribute("success",
                        "文件'" + file.getOriginalFilename() + "'上传成功!");
            }
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

    private Date getDeadLineByTemplateId(String templateId) {
        System.out.println(templateId);
        return templateService.getById(templateId).getDeadline();
    }

    /**
     * 根据模板id和学生id删除实验报告
     *
     * @param templateId 模板id
     * @param studentId  上传者id
     * @return main_student.html
     */
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

    Date getTemplateDeadlineById(String id) {
        return templateService.getById(id).getDeadline();
    }


}
