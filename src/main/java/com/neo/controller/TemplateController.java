package com.neo.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neo.domain.Report;
import com.neo.domain.Template;
import com.neo.exception.LargeFileException;
import com.neo.mapper.ClassesMapper;
import com.neo.mapper.ReportMapper;
import com.neo.service.ReportService;
import com.neo.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
public class TemplateController {

    @Autowired
    TemplateService templateService;

    @Autowired
    ClassesMapper classesMapper;

    @Autowired
    ReportService reportService;

    @Autowired
    ReportMapper reportMapper;

    /**
     * 最大上传限制
     */
    private static final Long MAX_UPLOAD_SIZE = 2 * 10 * 1024 * 1024L;

    /**
     * 上传实验报告模板Template实体
     * 将Template实体上传到数据库
     *
     * @param file               文件数据 -> MultipartFile
     * @param tno                上传教师id
     * @param cid                班级id
     * @param deadline           截止日期
     * @param redirectAttributes 重定向传值需要
     * @return main_teacher.html
     */
    @PostMapping("/uploadTemplate")
    public String singleFileUpload(@RequestParam(value = "file") MultipartFile file,
                                   @RequestParam(value = "tno", defaultValue = "undefined") String tno,
                                   @RequestParam(value = "class", defaultValue = "undefined") String cid,
                                   @RequestParam(value = "deadline")
                                   @DateTimeFormat(pattern = "yyyy-MM-dd") Date deadline,
                                   RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "请选择文件再上传!");
            return "redirect:mainTeacher";
        }
        try {
            // 取得文件并以Bytes方式保存
            byte[] bytes = file.getBytes();
            if (bytes.length > MAX_UPLOAD_SIZE) {
                throw new LargeFileException(MAX_UPLOAD_SIZE);
            }
            String uuid = UUID.randomUUID().toString();

            templateService.save(new Template(uuid,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    tno,
                    cid,
                    deadline,
                    bytes
            ));
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
        return "redirect:mainTeacher";
    }

    /**
     * 根据实验报告id删除实验报告
     *
     * @param templateId 实验报告id
     * @return main_teacher.html
     */
    @GetMapping("/deleteTemplate")
    String deleteTemplate(@RequestParam(value = "templateId") String templateId) {
        QueryWrapper<Report> reportQueryWrapper = new QueryWrapper<>();
        reportQueryWrapper.eq("report_template", templateId);
        reportMapper.delete(reportQueryWrapper);
        templateService.removeById(templateId);
        return "redirect:mainTeacher";
    }

}
