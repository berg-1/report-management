package com.neo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neo.domain.*;
import com.neo.mapper.ClassesCourseMapper;
import com.neo.mapper.ReportMapper;
import com.neo.mapper.StudentMapper;
import com.neo.mapper.TemplateMapper;
import com.neo.service.ClassesService;
import com.neo.service.CourseService;
import com.neo.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.*;

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

    @Autowired
    CourseService courseService;

    @Autowired
    ReportMapper reportMapper;

    @Autowired
    StudentMapper studentMapper;

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
            record.setClassId(classesService.getById(record.getClassId()).getName());
            record.setTemplateTeacher(String.format("%s(%s)", record.getTemplateTeacher(), teacher.getTname()));
        }

        // 查询教师所教班级
        QueryWrapper<ClassesCourse> classesCourseQueryWrapper = new QueryWrapper<>();
        classesCourseQueryWrapper.eq("teacher_id", teacher.getTno());
        List<ClassesCourse> classesCourses = classesCourseMapper.selectList(classesCourseQueryWrapper);
        HashMap<String, String> classesStringHashMap = new HashMap<>();

        for (ClassesCourse classesCourse : classesCourses) {
            // 教的班级的id
            String id = classesCourse.getClassId();
            // 教的课程id
            String courseId = classesCourse.getCourseId();
            String name = String.format("%s %s", classesService.getById(id).getName(), courseService.getById(courseId).getName());
            classesStringHashMap.put(name, id);
        }

        // 添加page信息到model
        model.addAttribute("templates", templates);
        // 添加classes到model
        model.addAttribute("classes", classesStringHashMap);
        return "main_teacher";
    }

    /**
     * 根据模板id和学生id获取实验报告提交情况
     *
     * @param templateId 模板id
     * @param className  课程名,后转化为课程id
     * @param model      存放提交和未提交学生的信息
     * @return 实验报告统计页面
     */
    @GetMapping(value = {"stats"})
    public String templateStats(@RequestParam(value = "templateId") String templateId,
                                @RequestParam(value = "classId") String className,
                                Model model) {
        className = getClassIdByName(className);
        List<Student> students = getClassStudents(className);
//        List<Student> submitted = new ArrayList<>();
        HashMap<Student, Report> submitted = new HashMap<>();
        List<Student> unSubmitted = new ArrayList<>();
        for (Student student : students) {
            student.setClassId(getClassNameById(student.getClassId()));
            Report report = getReportByTemplateIdAndStudentId(templateId, student.getSno());
            if (report != null) {
                submitted.put(student, report);
//                submitted.add(student);
            } else {
                unSubmitted.add(student);
            }
        }
        model.addAttribute("submitted", submitted);
        model.addAttribute("unSubmitted", unSubmitted);
        return "stat_page";
    }

    /**
     * 返回一个班的所有学生信息
     *
     * @param classId 班级id
     * @return 班级学生对象的List集合
     */
    public List<Student> getClassStudents(String classId) {
        QueryWrapper<Student> studentQueryWrapper = new QueryWrapper<>();
        studentQueryWrapper.eq("class_id", classId);
        return studentMapper.selectList(studentQueryWrapper);
    }

    /**
     * 根据班级ID获取班级名字
     *
     * @param classId 班级id
     * @return 班级名字
     */
    public String getClassNameById(String classId) {
        return classesService.getById(classId).getName();
    }

    /**
     * 根据班级名字获取班级ID
     *
     * @param className 班级名字
     * @return 班级id
     */
    public String getClassIdByName(String className) {
        return classesService.getOne(new QueryWrapper<Classes>().eq("name", className)).getCid();
    }

    public Report getReportByTemplateIdAndStudentId(String templateId, String studentId) {
        QueryWrapper<Report> reportQueryWrapper = new QueryWrapper<>();
        reportQueryWrapper.eq("report_template", templateId)
                .eq("uploader", studentId);
        return reportMapper.selectOne(reportQueryWrapper);
    }

    /**
     * 根据学生id和模板id获取学生提交的实验报告
     *
     * @param studentId  学生id
     * @param templateId 模板id
     * @return 学生提交的实验报告对象
     */
    public Report getReportByStudentIdAndTemplateId(String studentId, String templateId) {
        QueryWrapper<Report> reportQueryWrapper = new QueryWrapper<>();
        reportQueryWrapper.select("rid", "filename", "type", "uploader", "report_template", "upload_time")
                .eq("uploader", studentId)
                .eq("report_template", templateId);
        return reportMapper.selectOne(reportQueryWrapper);
    }

}
