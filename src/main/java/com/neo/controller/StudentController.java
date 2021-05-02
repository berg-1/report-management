package com.neo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neo.domain.Classes;
import com.neo.domain.Report;
import com.neo.domain.Student;
import com.neo.domain.Template;
import com.neo.mapper.ReportMapper;
import com.neo.mapper.TemplateMapper;
import com.neo.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Berg
 */
@Controller
public class StudentController {


    @Autowired
    StudentService studentService;

    @Autowired
    CourseService courseService;

    @Autowired
    ClassesService classesService;

    @Autowired
    TemplateMapper templateMapper;

    @Autowired
    ReportMapper reportMapper;

    @Autowired
    TeacherService teacherService;


    /**
     * 跳转
     *
     * @param session session内包含户用类(学生或老师,由Object转型即可)
     * @return 跳转到教师页面
     */
    @GetMapping(value = {"mainStudent", "mainStudent.html"})
    public String studentPage(@RequestParam(value = "pn", defaultValue = "1") Integer pn,
                              HttpSession session,
                              Model model) {
        Student student = (Student) session.getAttribute("loginUser");
        String classId = student.getClassId();
        QueryWrapper<Template> wrapper = new QueryWrapper<>();
        wrapper.eq("class_id", classId);
        List<Template> templates = templateMapper.selectList(wrapper);
        List<Template> submitted = new ArrayList<>();
        List<Template> unsubmitted = new ArrayList<>();
        for (Template template : templates) {
            System.out.println(template.getTemplateId());
            QueryWrapper<Report> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("rid", template.getTemplateId())
                    .eq("uploader", student.getSno());
            Integer integer = reportMapper.selectCount(queryWrapper);
            template.setData(null);
            template.setClassId(classesService.getById(template.getClassId()).getName());
            template.setTemplateTeacher(teacherService.getById(template.getTemplateTeacher()).getTname());
            if (integer > 0) {
                System.out.println("已提交该模板");
                submitted.add(template);
            } else {
                System.out.println("未提交模板");
                unsubmitted.add(template);
            }
        }
        model.addAttribute("submitted", submitted);
        model.addAttribute("unsubmitted", unsubmitted);
        return "main_student";
    }

    @GetMapping("/getClassName")
    String getClassName(@RequestParam(value = "classId") String cid) {
        Classes c = classesService.getById(cid);
        return c.getName();
    }
}
