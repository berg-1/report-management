package com.neo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.neo.domain.*;
import com.neo.mapper.StudentMapper;
import com.neo.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @author Berg
 */
@Controller
@Slf4j
public class IndexController {

    @Autowired
    StudentService studentService;

    @Autowired
    TeacherService teacherService;

    @Autowired
    TemplateService templateService;

    @Autowired
    RedisService redisService;

    @Autowired
    ReportService reportService;

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
    public String main(User user, HttpSession session, Model model) {
        String username = user.getUsername();
        String userPasswd = user.getPassword();
        log.info("user: {}", user);
        Student id = studentService.getById(username);
        session.setMaxInactiveInterval(2 * 60 * 60);  // 60分钟后失效
        if (id != null && (id.getPassword().equals(userPasswd))) {
            log.debug("学生登录id={}", id.getSno());
            session.setAttribute("loginUser", id);
            return "redirect:/mainStudent";
        } else if (teacherService.getById(username) != null && (teacherService.getById(username).getPassword().equals(userPasswd))) {
            log.debug("教师登录id={}", username);
            session.setAttribute("loginUser", teacherService.getById(username));
            QueryWrapper<Template> wrapper = new QueryWrapper<>();
            wrapper.select("template_id", "name", "type", "template_teacher", "class_id", "course_id", "deadline")
                    .eq("template_teacher", username);
            List<Template> teacherTemplates = templateService.list(wrapper);
            for (Template teacherTemplate : teacherTemplates) {
                int count = getReportCountByTemplateId(teacherTemplate.getTemplateId());
                redisService.hSet(username, teacherTemplate.getTemplateId(), String.valueOf(count));
            }
            return "redirect:/mainTeacher";
        } else {
            model.addAttribute("msg", "账号或密码错误");
            // 登录失败,回到登录页
            return "login";
        }
    }

    /**
     * 退出登录
     *
     * @param session session 存放登录用户信息
     * @return login页面
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("loginUser");
        return "login";
    }

    /**
     * 跳转到更新密码页面
     *
     * @param id    更新学生或教师的id
     * @param model model
     * @return change_passwd.html
     */
    @GetMapping("/changePasswd")
    public String changePassWord(@RequestParam(value = "id", defaultValue = "") String id,
                                 Model model) {
        model.addAttribute("id", id);
        return "change_passwd";
    }

    /**
     * 更新密码
     *
     * @param userId    id,学生或教师id
     * @param passwd    原密码
     * @param passwdNew 新密码
     * @param model     model,返回更新信息
     * @return login页面
     */
    @PostMapping("/updatePasswd")
    public String updatePasswd(@RequestParam String userId,
                               @RequestParam String passwd,
                               @RequestParam String passwdNew,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        log.debug("Username:{},Password:{},new Password:{}", userId, passwd, passwdNew);
        Student id = studentService.getById(userId);
        if (id != null && (id.getPassword().equals(passwd))) {
            log.debug("学生修改密码 id={}", id.getSno());
            UpdateWrapper<Student> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("sno", userId)
                    .eq("password", passwd)
                    .set("password", passwdNew);
            boolean update = studentService.update(null, updateWrapper);
            if (update) {
                model.addAttribute("success", "修改密码成功!");
            } else {
                model.addAttribute("msg", "修改密码失败!");
            }
            return "login";
        } else if (teacherService.getById(userId) != null && (teacherService.getById(userId).getPassword().equals(passwd))) {
            log.debug("教师修改密码 id={}", userId);
            UpdateWrapper<Teacher> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("tno", userId)
                    .eq("password", passwd)
                    .set("password", passwdNew);
            boolean update = teacherService.update(updateWrapper);
            if (update) {
                model.addAttribute("success", "修改密码成功!");
            } else {
                model.addAttribute("msg", "修改密码失败!");
            }
            return "login";
        } else {
            redirectAttributes.addFlashAttribute("msg", "账号或密码错误,请重试");
            return "redirect:changePasswd";
        }
    }

    private int getReportCountByTemplateId(String templateId) {
        QueryWrapper<Report> wrapper = new QueryWrapper<>();
        wrapper.eq("report_template", templateId);
        return reportService.count(wrapper);
    }

}
