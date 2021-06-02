package com.neo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neo.Utils.MimeTypes;
import com.neo.domain.Report;
import com.neo.domain.Template;
import com.neo.exception.LargeFileException;
import com.neo.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @Autowired
    ClassesService classesService;

    @Autowired
    CourseService courseService;

    @Autowired
    StudentService studentService;

    /**
     * 最大上传限制
     */
    private static final Long MAX_UPLOAD_SIZE = 2 * 10 * 1024 * 1024L;
    private static final String ACCEPT_FORMAT = ".pdf";

    /**
     * 上传一个Report实体到数据库
     *
     * @param file               文件数据 -> MultipartFile
     * @param sno                上传者id
     * @param templateId         模板id
     * @param redirectAttributes 重定向传值需要
     * @return mian_student.html
     */
    @PostMapping("/uploadReport")
    public String singleFileUpload(@RequestParam(value = "file") MultipartFile file,
                                   @RequestParam(value = "sno", defaultValue = "undefined") String sno,
                                   @RequestParam(value = "templateId", defaultValue = "undefined") String templateId,
                                   RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("messageId", templateId);
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "请选择文件再上传!");
            return "redirect:mainStudent";
        }
        assert file.getOriginalFilename() != null;
        if (!ACCEPT_FORMAT.equalsIgnoreCase(
                file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")))) {
            redirectAttributes.addFlashAttribute("message", "请转换为PDF文件后再上传!");
            return "redirect:mainStudent";
        }
        try {
            // 取得文件并以Bytes方式保存
            byte[] bytes = file.getBytes();
            if (bytes.length > MAX_UPLOAD_SIZE) {
                throw new LargeFileException(MAX_UPLOAD_SIZE);
            }
            String uuid = UUID.randomUUID().toString();
            String fileName = file.getOriginalFilename();
            String type = file.getContentType();
            Template template = getTemplateNoData(templateId);
            String courseId = template.getCourseId();
            String classId = template.getClassId();
            String templateName = template.getName();
            Date uploadDate = new Date();
            boolean isLate = uploadDate.before(template.getDeadline());
            reportService.save(new Report(uuid,
                    fileName,
                    type,
                    sno,
                    templateId,
                    courseId,
                    uploadDate,
                    bytes,
                    isLate));
            String savePath = getSavePath(courseId, classId, templateName, sno, type);
            File saveFolderPath = new File(savePath.substring(0, savePath.lastIndexOf("/")));
            if (!saveFolderPath.isDirectory() && !saveFolderPath.exists()) {
                boolean mkdirs = saveFolderPath.mkdirs();
                if (mkdirs) {
                    log.info("创建文件夹:{}", file);
                } else {
                    log.info("文件夹{}创建失败", file);
                }
            }
            Path path = Paths.get(savePath);
            Files.write(path, bytes);
            log.info("保存文件{}", savePath);
            if (!isLate) {
                log.info("学生提交时间超出截至日期!id={},templateId={}", sno, templateId);
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

    /**
     * 根据模板Id获取一个没有数据的模板对象
     *
     * @param templateId template id
     * @return Template实体
     */
    private Template getTemplateNoData(String templateId) {
        QueryWrapper<Template> wrapper = new QueryWrapper<>();
        wrapper.select("template_id", "name", "type", "template_teacher", "class_id", "course_id", "deadline")
                .eq("template_id", templateId);
        return templateService.getOne(wrapper);
    }

    private String getClassIdByTemplateId(String templateId) {
        QueryWrapper<Template> wrapper = new QueryWrapper<>();
        wrapper.select("template_id", "class_id")
                .eq("template_id", templateId);
        return templateService.getOne(wrapper).getClassId();
    }

    private Date getDeadLineByTemplateId(String templateId) {
        QueryWrapper<Template> wrapper = new QueryWrapper<>();
        wrapper.select("template_id", "deadline")
                .eq("template_id", templateId);
        return templateService.getOne(wrapper).getDeadline();
    }

    private String getTemplateCourseByTemplateId(String templateId) {
        QueryWrapper<Template> wrapper = new QueryWrapper<>();
        wrapper.select("template_id", "courseId")
                .eq("template_id", templateId);
        return templateService.getById(templateId).getCourseId();
    }

    private String getTemplateNameByTemplateId(String templateId) {
        QueryWrapper<Template> wrapper = new QueryWrapper<>();
        wrapper.select("template_id", "name")
                .eq("template_id", templateId);
        return templateService.getOne(wrapper).getName();
    }


    /**
     * 保存文件的路径
     *
     * @param courseId     课程id
     * @param classId      班级id
     * @param templateName 实验名
     * @param studentId    学生id
     * @return 返回保存文件的路径 -> ./课程名/班级名/实验名/学号.姓名.pdf
     */
    private String getSavePath(String courseId, String classId, String templateName, String studentId, String type) {
        return String.format("./%s/%s/%s/%s.%s.%s",
                courseService.getById(courseId).getName(),
                classesService.getById(classId).getName(),
                templateName.substring(0, templateName.lastIndexOf(".")),
                studentId.substring(12),
                studentService.getById(studentId).getName(),
                MimeTypes.getDefaultExt(type));
    }

    /**
     * 根据模板id和学生id删除实验报告 !Deprecated
     *
     * @param templateId 模板id
     * @param studentId  上传者id
     * @return main_student.html
     */
    @GetMapping("/deleteReportDeprecated")
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

    /**
     * 根据Id删除实验报告
     *
     * @param reportId 实验报告id
     * @return main_student.html
     */
    @GetMapping("/deleteReport")
    String deleteTemplate(@RequestParam("reportId") String reportId) {
        reportService.removeById(reportId);
        return "redirect:mainStudent";
    }


}
