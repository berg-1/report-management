package com.neo.controller;

import com.neo.domain.Student;
import com.neo.service.StudentService;
import com.neo.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;

/**
 * @author Berg
 */
@Controller
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

    @PostMapping("/login")
    public String main(Student user, HttpSession session, Model model) {
        Student id = ss.getById(user.getSno());
        if (id != null) {
            System.out.println("是学生");
            session.setAttribute("loginUser", id);
        } else if (ts.getById(user.getSno()) != null) {
            System.out.println("是老师");
            session.setAttribute("loginUser", ts.getById(user.getSno()));
        } else {
            System.out.println("请输入正确的用户名");
        }

//        if (StringUtils.hasLength(user.getUsername()) && "123456".equals(user.getPassword())) {
//            // 保存登录成功的用户
//            session.setAttribute("loginUser", user);
//            // 登录成功, 重定向到main.html页面  重定向防止表单重复提交
//            return "redirect:/main";
//        } else {
//            model.addAttribute("msg", "账号或密码错误");
//            // 登录失败,回到登录页
//            return "login";
//        }
        return "login";
    }

}
