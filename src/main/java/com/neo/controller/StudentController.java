package com.neo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neo.domain.Report;
import com.neo.domain.Student;
import com.neo.domain.Template;
import com.neo.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Berg
 */
@Slf4j
@Controller
public class StudentController {


    @Autowired
    StudentService studentService;

    @Autowired
    CourseService courseService;

    @Autowired
    ClassesService classesService;

    @Autowired
    TemplateService templateService;

    @Autowired
    ReportService reportService;

    @Autowired
    TeacherService teacherService;

    @Autowired
    RedisService redisService;


    /**
     * 跳转
     *
     * @param session session内包含户用类(学生或老师,由Object转型即可)
     * @return 跳转到教师页面
     */
    @GetMapping(value = {"mainStudent", "mainStudent.html"})
    public String studentPage(HttpSession session, Model model) {
        Student student = (Student) session.getAttribute("loginUser");
        String classId = student.getClassId();
        List<Template> unSubmitted = new ArrayList<>();
        HashMap<Template, Report> submitted = new HashMap<>();

        QueryWrapper<Template> wrapper = new QueryWrapper<>();
        wrapper.select("template_id", "name", "type", "template_teacher", "class_id", "deadline", "course_id")
                .eq("class_id", classId);
        List<Template> templates = templateService.list(wrapper);

        QueryWrapper<Report> wrapper1 = new QueryWrapper<>();
        wrapper1.select("rid", "filename", "type", "uploader", "report_template", "upload_time", "status", "course_id")
                .eq("uploader", student.getSno());
        List<Report> reportsList = reportService.list(wrapper1);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        for (Template template : templates) {
            String templateId = template.getTemplateId();
            List<Report> r = reportsList.stream()
                    .filter(m -> m.getReportTemplate().equals(templateId))
                    .collect(Collectors.toList());
            template.setCourseId(redisService.hGet("course", template.getCourseId()));
            if (r.isEmpty()) {
                unSubmitted.add(template);
            } else if (r.size() == 1) {
                submitted.put(template, r.get(0));
            } else {
                for (int i = 1; i < r.size(); i++) {
                    reportService.removeById(r.get(i).getRid());
                    log.info("删除相同的实验报告:{}", r.get(i).getFilename());
                }
            }

        }

        stopWatch.stop();
        log.info("Student Login Time Cost: {} ms", stopWatch.getLastTaskTimeMillis());
        model.addAttribute("submitted", submitted);
        model.addAttribute("unsubmitted", unSubmitted);
        return "main_student";
    }


    Report getOne(String templateId, String uploader) {
        QueryWrapper<Report> getOneWrapper = new QueryWrapper<>();
        getOneWrapper.select("rid", "filename", "type", "uploader", "upload_time", "report_template", "status", "course_id")
                .eq("report_template", templateId)
                .eq("uploader", uploader);
        return reportService.getOne(getOneWrapper);
    }


    String getCourseNameById(String courseId) {
        return courseService.getById(courseId).getName();
    }

    String getClassNameById(String cid) {
        return classesService.getById(cid).getName();
    }

    String getTemplateTeacherById(String tno) {
        return teacherService.getById(tno).getTname();
    }
}
