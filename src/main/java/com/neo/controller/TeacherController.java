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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Berg
 */
@Slf4j
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
        return "new_main_teacher";
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
        HashMap<Student, Report> submitted = new HashMap<>();
        List<Student> unSubmitted = new ArrayList<>();
        for (Student student : students) {
            student.setClassId(getClassNameById(student.getClassId()));
            Report report = getReportByTemplateIdAndStudentId(templateId, student.getSno());
            if (report != null) {
                submitted.put(student, report);
            } else {
                unSubmitted.add(student);
            }
        }
        model.addAttribute("submitted", submitted);
        model.addAttribute("unSubmitted", unSubmitted);
        model.addAttribute("templateId", templateId);
        model.addAttribute("classId", className);
        return "stat_page";
    }

    /**
     * 批量导出一个班的实验报告 -> D:/实验报告下载/班级名 实验模板名.zip
     *
     * @param templateId 模板id
     * @param classId    班级id
     * @param response   返回
     */
    @GetMapping("/export")
    public void exportedReportsInZip(@RequestParam(value = "templateId") String templateId,
                                     @RequestParam(value = "classId") String classId,
                                     HttpServletResponse response) {
        ArrayList<Report> reports = new ArrayList<>();
        List<Student> students = getClassStudents(classId);
        for (Student student : students) {
            student.setClassId(getClassNameById(student.getClassId()));
            Report report = getReportByTemplateIdAndStudentIdWithData(templateId, student.getSno());
            if (report != null) {
                reports.add(report);
            }
        }
        //生成zip文件名 去掉templateName的后缀名
        String className = getClassNameById(classId);
        String templateName = getTemplateNameById(templateId);
        // 压缩文件的名字
        String zipName = String.format("%s %s.zip", className, templateName.substring(0, templateName.lastIndexOf('.')));
        // 压缩文件下载的位置/缓存位置
        String strZipPath = "D:/实验报告下载/" + zipName;
        File file = new File("D:/实验报告下载/");
        //文件存放位置目录不存在就创建
        if (!file.isDirectory() && !file.exists()) {
            if (file.mkdirs()) {
                log.info("创建文件夹:{}", file);
            } else {
                log.info("文件夹{}创建失败", file);
            }
        }
        try {
            //通过response的outputStream输出文件
            ServletOutputStream outputStream = response.getOutputStream();
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(strZipPath));
            for (Report report : reports) {
                log.info("下载,报告名:{}", report.getFilename());
                String filename = String.format("%s.%s.%s%s",
                        templateName.substring(0, templateName.lastIndexOf('.')),
                        report.getUploader().substring(12),
                        getStudentNameById(report.getUploader()),
                        report.getFilename().substring(report.getFilename().lastIndexOf('.')));
                out.putNextEntry(new ZipEntry(filename));
                // 读入需要下载的文件的内容，打包到zip文件
                out.write(report.getData());
                out.closeEntry();
            }
            out.close();
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(strZipPath));
            response.setContentType("application/x-msdownload");
            response.setHeader("Content-disposition", "attachment; filename="
                    + new String(zipName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
            //将输入流的数据拷贝到输入流输出
            FileCopyUtils.copy(bis, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //删除文件或者文件夹下所有文件
        removeDir(file);
    }

    /**
     * 根据模板Id,返回模板名字
     *
     * @param id 模板id
     * @return 模板id所对应的名字
     */
    private String getTemplateNameById(String id) {
        return templateService.getById(id).getName();
    }

    /**
     * 删除目录下的所有文件
     *
     * @param dir 删除目录
     */
    private void removeDir(File dir) {
        File[] files = dir.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {
                removeDir(file);
            } else {
                if (file.delete()) {
                    log.info("删除文件{}", file.getName());
                } else {
                    log.info("删除文件{}失败", file.getName());
                }
            }
        }
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

    /**
     * 根据模板Id和学生Id来定位学生提交的报告
     *
     * @param templateId 模板Id
     * @param studentId  学生Id
     * @return 模板Id和学生Id所对应的实验报告
     */
    public Report getReportByTemplateIdAndStudentId(String templateId, String studentId) {
        QueryWrapper<Report> reportQueryWrapper = new QueryWrapper<>();
        reportQueryWrapper.select("rid", "filename", "type", "uploader", "report_template", "upload_time")
                .eq("report_template", templateId)
                .eq("uploader", studentId);
        return reportMapper.selectOne(reportQueryWrapper);
    }

    /**
     * 根据模板Id和学生Id来定位学生提交的报告
     *
     * @param templateId 模板Id
     * @param studentId  学生Id
     * @return 模板Id和学生Id所对应的实验报告
     */
    public Report getReportByTemplateIdAndStudentIdWithData(String templateId, String studentId) {
        QueryWrapper<Report> reportQueryWrapper = new QueryWrapper<>();
        reportQueryWrapper.select("rid", "filename", "type", "data", "uploader", "report_template", "upload_time")
                .eq("report_template", templateId)
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

    /**
     * 根据学生id获取学生姓名
     *
     * @param id 学生id
     * @return 学生id对应的学生姓名
     */
    public String getStudentNameById(String id) {
        return studentMapper.selectById(id).getName();
    }

}
