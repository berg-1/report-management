package com.neo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neo.domain.Report;
import com.neo.domain.Template;
import com.neo.exception.LargeFileException;
import com.neo.service.RedisService;
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

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * @author Berg
 */
@Slf4j
@Controller
public class TemplateController {

    @Autowired
    TemplateService templateService;


    @Autowired
    ReportService reportService;

    @Autowired
    RedisService redisService;


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
    public String singleFileUpload(@RequestParam(value = "name") String name,
                                   @RequestParam(value = "file") MultipartFile file,
                                   @RequestParam(value = "tno", defaultValue = "undefined") String tno,
                                   @RequestParam(value = "class", defaultValue = "undefined") String cid,
                                   @RequestParam(value = "deadline") String deadline,
                                   RedirectAttributes redirectAttributes) {
        try {
            Date deadlineFormat = new SimpleDateFormat("yyyy年MM月dd日 hh:mm").parse(deadline);
            byte[] bytes;
            String type;
            // 取得文件并以Bytes方式保存
            if (file.isEmpty()) {
                type = "text/plain";
                bytes = name.getBytes(StandardCharsets.UTF_8);
            } else {
                type = file.getContentType();
                bytes = file.getBytes();
            }
            if (bytes.length > MAX_UPLOAD_SIZE) throw new LargeFileException(MAX_UPLOAD_SIZE);
            String uuid = UUID.randomUUID().toString();
            name = name.replace(' ', '.');
            String[] courseIdAndCid = cid.split("@");
            String courseId = courseIdAndCid[0];
            String classId = courseIdAndCid[1];
            templateService.save(new Template(uuid, name, type, tno, classId, deadlineFormat, courseId, bytes));
            redisService.hSet(tno, uuid, "0");
            redirectAttributes.addFlashAttribute("success", "实验'" + name + "'上传成功!");
            redirectAttributes.addAttribute("classId", classId);
            redirectAttributes.addAttribute("courseId", courseId);
        } catch (LargeFileException largeFileException) {
            redirectAttributes.addFlashAttribute("message", String.format("请将文件控制在%dMB内!",
                    largeFileException.getMaxSize() / 1048576));
        } catch (Exception s) {
            redirectAttributes.addFlashAttribute("message", "上传失败!");
            s.printStackTrace();
        }
        return "redirect:mainTeacher";
    }


    @PostMapping("/modifyTemplate")
    public String modifyTemplate(@RequestParam(value = "templateId") String templateId,
                                 @RequestParam(value = "name") String name,
                                 MultipartFile file,
                                 @RequestParam(value = "tno", defaultValue = "undefined") String tno,
                                 @RequestParam(value = "deadline") String deadline,
                                 RedirectAttributes attributes) {
        log.info("file == null : {}", file.isEmpty());
        Template origin = templateService.getById(templateId);
        try {
            Date deadlineFormat = new SimpleDateFormat("yyyy年MM月dd日 hh:mm").parse(deadline);
            byte[] bytes;
            String type;
            if (file.isEmpty()) {
                type = origin.getType();
                bytes = origin.getData();
            } else {
                type = file.getContentType();
                bytes = file.getBytes();
            }
            if (bytes.length > MAX_UPLOAD_SIZE) {
                throw new LargeFileException(MAX_UPLOAD_SIZE);
            }
            name = name.replace(' ', '.');
            templateService.updateById(new Template(templateId, name, type, tno,
                    origin.getClassId(),
                    deadlineFormat,
                    origin.getCourseId(),
                    bytes));
            attributes.addFlashAttribute("success", "实验'" + name + "'修改成功!");
            attributes.addAttribute("classId", origin.getClassId());
            attributes.addAttribute("courseId", origin.getCourseId());
        } catch (LargeFileException largeFileException) {
            attributes.addFlashAttribute("message", String.format(
                    "文件过大,请将文件控制在%dMB内!",
                    largeFileException.getMaxSize() / 1048576
            ));
        } catch (Exception s) {
            attributes.addFlashAttribute("message", "修改失败!");
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
    String deleteTemplate(@RequestParam(value = "templateId") String templateId, RedirectAttributes attributes) {
        QueryWrapper<Report> reportQueryWrapper = new QueryWrapper<>();
        reportQueryWrapper.eq("report_template", templateId);
        QueryWrapper<Template> wrapper = new QueryWrapper<>();
        wrapper.select("course_id", "class_id").eq("template_id", templateId);
        Template one = templateService.getOne(wrapper);
        reportService.remove(reportQueryWrapper);
        templateService.removeById(templateId);
        attributes.addAttribute("classId", one.getClassId());
        attributes.addAttribute("courseId", one.getCourseId());
        return "redirect:mainTeacher";
    }

}
