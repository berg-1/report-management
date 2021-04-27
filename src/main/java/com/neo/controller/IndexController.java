package com.neo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neo.domain.Student;
import com.neo.domain.Teacher;
import com.neo.domain.Template;
import com.neo.mapper.TemplateMapper;
import com.neo.service.StudentService;
import com.neo.service.TeacherService;
import com.neo.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @author Berg
 */
@Controller
public class IndexController {

    @Autowired
    StudentService ss;

    @Autowired
    TeacherService ts;

    @Autowired
    TemplateService templateService;

    /**
     * 来登录页
     *
     * @return 登录页地址
     */
    @GetMapping(value = {"/", "/login"})
    public String loginPage() {
        return "login";
    }

    /**
     * 登录认证
     *
     * @param user    登录用户,包含用户名和密码
     * @param session session
     * @param model   model
     * @return 验证完毕, 跳转页面
     */
    @PostMapping("/login")
    public String main(Student user, HttpSession session, Model model) {
        Student id = ss.getById(user.getSno());
        if (id != null && (id.getPassword().equals(user.getPassword()))) {
            System.out.println("登录的是学生");
            session.setAttribute("loginUser", id);
            return "redirect:/mainStudent";
        } else if (ts.getById(user.getSno()) != null && (ts.getById(user.getSno()).getPassword().equals(user.getPassword()))) {
            System.out.println("登陆的是老师");
            session.setAttribute("loginUser", ts.getById(user.getSno()));
            return "redirect:/mainTeacher";
        } else {
            model.addAttribute("msg", "账号或密码错误");
            // 登录失败,回到登录页
            return "login";
        }
    }

    @Autowired
    TemplateMapper tm;

    /**
     * 跳转
     *
     * @param session session内包含户用类(学生或老师,由Object转型即可)
     * @return 跳转到教师页面
     */
    @GetMapping(value = {"mainStudent", "mainStudent.html"})
    public String studentPage(HttpSession session) {
        Student student = (Student) session.getAttribute("loginUser");
        return "main_student";
    }

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
        queryWrapper.eq("template_teacher", teacher.getTno());
        Page<Template> userPage = new Page<>(pn, 2);
        // 分页查询结果
        Page<Template> templates = templateService.page(userPage, queryWrapper);
        // 添加page信息到model
        model.addAttribute("templates", templates);
        return "main_teacher";
    }

}
