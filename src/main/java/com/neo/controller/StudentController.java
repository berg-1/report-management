package com.neo.controller;

import com.neo.domain.Student;
import com.neo.service.ClassesService;
import com.neo.service.CourseService;
import com.neo.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

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

        return "main_student";
    }
}
