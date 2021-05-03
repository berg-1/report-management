package com.neo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neo.domain.Classes;
import com.neo.domain.ClassesCourse;
import com.neo.domain.Teacher;
import com.neo.domain.Template;
import com.neo.mapper.ClassesCourseMapper;
import com.neo.mapper.TemplateMapper;
import com.neo.service.ClassesService;
import com.neo.service.TemplateService;
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
public class TeacherController {

    @Autowired
    TemplateMapper tm;

    @Autowired
    TemplateService templateService;

    @Autowired
    ClassesCourseMapper classesCourseMapper;

    @Autowired
    ClassesService classesService;

    /**
     * @param pn      查询第几页的数据, 默认值为1
     * @param session session session内包含用户类(学生或老师,由Object转型即可)
     * @param model   model
     * @return 跳转到学生页面
     */
    @GetMapping(value = {"mainTeacher", "mainTeacher.html"})
    public String teacherPage(@RequestParam(value = "pn", defaultValue = "1") Integer pn,
                              HttpSession session,
                              Model model) {
        Teacher teacher = (Teacher) session.getAttribute("loginUser");
        QueryWrapper<Template> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("template_id", "name", "type", "template_teacher", "class_id", "deadline").
                eq("template_teacher", teacher.getTno());
        Page<Template> userPage = new Page<>(pn, 10);
        // 分页查询结果
        Page<Template> templates = templateService.page(userPage, queryWrapper);
        for (Template record : templates.getRecords()) {
            record.setData(null);
            record.setTemplateTeacher(String.format("%s(%s)", record.getTemplateTeacher(), teacher.getTname()));
        }

        // 查询教师所教班级
        QueryWrapper<ClassesCourse> classesCourseQueryWrapper = new QueryWrapper<>();
        classesCourseQueryWrapper.eq("teacher_id", teacher.getTno());
        List<ClassesCourse> classesCourses = classesCourseMapper.selectList(classesCourseQueryWrapper);
        ArrayList<Classes> classes = new ArrayList<>();
        for (ClassesCourse classesCourse : classesCourses) {
            classes.add(classesService.getById(classesCourse.getClassId()));
        }

        // 添加page信息到model
        model.addAttribute("templates", templates);
        // 添加classes到model
        model.addAttribute("classes", classes);
        return "main_teacher";
    }

}
