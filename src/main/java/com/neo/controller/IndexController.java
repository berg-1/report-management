package com.neo.controller;

import com.neo.domain.Student;
import com.neo.service.StudentService;
import com.neo.service.TeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;

/**
 * @author Berg
 */
@Controller
@Slf4j
public class IndexController {

    @Autowired
    StudentService ss;

    @Autowired
    TeacherService ts;

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
            log.info("学生登录id={}", id.getSno());
            session.setAttribute("loginUser", id);
            return "redirect:/mainStudent";
        } else if (ts.getById(user.getSno()) != null && (ts.getById(user.getSno()).getPassword().equals(user.getPassword()))) {
            log.info("教师登录id={}", user.getSno());
            session.setAttribute("loginUser", ts.getById(user.getSno()));
            return "redirect:/mainTeacher";
        } else {
            model.addAttribute("msg", "账号或密码错误");
            // 登录失败,回到登录页
            return "login";
        }
    }


}
