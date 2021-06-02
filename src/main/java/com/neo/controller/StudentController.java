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
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        QueryWrapper<Template> wrapper = new QueryWrapper<>();
        wrapper.select("template_id", "name", "type", "template_teacher", "class_id", "deadline", "course_id").
                eq("class_id", classId);
        List<Template> templates = templateService.list(wrapper);
        List<Template> unSubmitted = new ArrayList<>();
        HashMap<Template, Report> submitted = new HashMap<>();

        for (Template template : templates) {
            QueryWrapper<Report> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("report_template", template.getTemplateId())
                    .eq("uploader", student.getSno());
            int integer = reportService.count(queryWrapper);
            template.setCourseId(getCourseNameById(template.getCourseId()));
            template.setTemplateTeacher(getTemplateTeacherById(template.getTemplateTeacher()));
            if (integer > 0) {
                Report one = reportService.getOne(queryWrapper);
                one.setData(null);
                submitted.put(template, one);
                log.debug("The one:{}", one.getFilename());
            } else {
                unSubmitted.add(template);
            }
        }
        model.addAttribute("submitted", submitted);
        model.addAttribute("unsubmitted", unSubmitted);
        return "main_student";
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
