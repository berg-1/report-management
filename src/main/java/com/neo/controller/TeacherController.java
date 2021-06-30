package com.neo.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neo.Utils.MimeTypes;
import com.neo.domain.*;
import com.neo.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
    TemplateService templateService;

    @Autowired
    ClassesCourseService classesCourseService;

    @Autowired
    ClassesService classesService;

    @Autowired
    CourseService courseService;

    @Autowired
    ReportService reportService;

    @Autowired
    StudentService studentService;

    /**
     * 去教师页面
     *
     * @param session session,存放用户登录信息,包括教师
     * @param model   将教师所教班级存放到model中,方便前端显示
     * @return new_main_teacher.html
     */
    @GetMapping(value = {"mainTeacher", "mainTeacher.html"})
    public String teacherPage(HttpSession session, Model model,
                              @RequestParam(defaultValue = "*") String classId,
                              @RequestParam(defaultValue = "*") String courseId) {
        Teacher teacher = (Teacher) session.getAttribute("loginUser");
        List<Template> templates;
        QueryWrapper<Template> wrapper = new QueryWrapper<>();
        if (classId.equals("*")) {
            wrapper.select("template_id", "name", "type", "template_teacher", "class_id", "course_id", "deadline")
                    .eq("template_teacher", teacher.getTno())
                    .orderByAsc("name");
        } else {
            wrapper.select("template_id", "name", "type", "template_teacher", "class_id", "course_id", "deadline")
                    .eq("class_id", classId)
                    .eq("course_id", courseId)
                    .orderByAsc("name");
        }
        templates = templateService.list(wrapper);
        for (Template template : templates) {
            int c = getReportCountByTemplateId(template.getTemplateId());
            template.setType(String.valueOf(c));
        }
        HashMap<String, String> classesStringHashMap = getTeacherClasses(teacher.getTno());
        // 添加classes到model
        model.addAttribute("templates", templates);
        model.addAttribute("classes", classesStringHashMap);
        return "new_main_teacher";
    }

    private int getReportCountByTemplateId(String templateId) {
        QueryWrapper<Report> wrapper = new QueryWrapper<>();
        wrapper.eq("report_template", templateId);
        return reportService.count(wrapper);
    }

    /**
     * 去教师页面
     *
     * @param pn      查询第几页的数据, 默认值为1
     * @param session session session内包含用户类(学生或老师,由Object转型即可)
     * @param model   model
     * @return 跳转到教师页面 main_teacher.html
     */
    @GetMapping(value = {"mainTeacherOld", "mainTeacherOld.html"})
    public String oldTeacherPage(@RequestParam(value = "pn", defaultValue = "1") Integer pn,
                                 HttpSession session,
                                 Model model) {
        Teacher teacher = (Teacher) session.getAttribute("loginUser");
        String teacherId = teacher.getTno();
        Page<Template> templatePagination = getTeacherTemplatePagination(pn, teacherId);
        HashMap<String, String> classes = getTeacherClasses(teacherId);
        model.addAttribute("templates", templatePagination);
        model.addAttribute("classes", classes);
        return "main_teacher";
    }

    @RequestMapping("nodes")
    @GetMapping
    @ResponseBody
    public List<Node> getCoursesAndClasses(HttpSession session) {
        Teacher teacher = (Teacher) session.getAttribute("loginUser");
        return getNodeList(teacher.getTno());
    }

    @RequestMapping("templates")
    @GetMapping
    @ResponseBody
    public JSONObject getNamesByTemplateId(@RequestParam String classId,
                                           @RequestParam String courseId) {
        JSONObject stat = new JSONObject();
        JSONObject ts = new JSONObject();
        QueryWrapper<Template> wrapper = new QueryWrapper<>();
        wrapper.select("template_id", "name", "type", "template_teacher", "class_id", "course_id", "deadline")
                .eq("class_id", classId)
                .eq("course_id", courseId)
                .orderByAsc("name");
        List<Template> templates = templateService.list(wrapper);
        for (Template template : templates) {
            ts.put(template.getTemplateId(), template);
        }
        stat.put("templates", ts);
        return stat;
    }


    /**
     * 根据模板id和学生id获取实验报告提交情况
     *
     * @param templateId 模板id
     * @param model      存放提交和未提交学生的信息
     * @return 实验报告统计页面
     */
    @GetMapping(value = {"stats"})
    public String templateStats(@RequestParam(value = "templateId") String templateId, Model model) {
        String classId = templateService.getById(templateId).getClassId();
        List<Student> students = getClassStudents(classId);

        HashMap<Student, Report> submitted = new HashMap<>(30);
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
        return "stat_page";
    }

    /**
     * 批量导出一个班的实验报告 -> D:/实验报告下载/班级名 实验模板名.zip
     *
     * @param templateId 模板id
     * @param response   返回
     */
    @GetMapping("/export")
    public void exportedReportsInZip(@RequestParam(value = "templateId") String templateId,
                                     HttpServletResponse response) {
        ArrayList<Report> reports = new ArrayList<>();
        String classId = templateService.getById(templateId).getClassId();
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
        String zipName = String.format("%s.%s.zip", className, templateName.substring(0, templateName.lastIndexOf('.')));
        // 压缩文件下载的位置/缓存位置
        String strZipPath = "./实验报告下载/" + zipName;
        File file = new File("./实验报告下载/");
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
                log.debug("下载,报告名:{}", report.getFilename());
                String filename = String.format("%s.%s.%s",
                        report.getUploader().substring(12),
                        getStudentNameById(report.getUploader()),
                        MimeTypes.getDefaultExt(report.getType()));
                out.putNextEntry(new ZipEntry(filename));
                // 读入需要下载的文件的内容，打包到zip文件
                out.write(report.getData());
                out.closeEntry();
            }
            out.close();
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(strZipPath));
            response.setContentType("application/x-msdownload");
            response.setHeader("Content-disposition", "attachment;filename="
                    + new String(zipName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
            // 将输入流的数据拷贝到输入流输出
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
    private List<Student> getClassStudents(String classId) {
        QueryWrapper<Student> studentQueryWrapper = new QueryWrapper<>();
        studentQueryWrapper.eq("class_id", classId);
        return studentService.list(studentQueryWrapper);
    }

    /**
     * 根据班级ID获取班级名字
     *
     * @param classId 班级id
     * @return 班级名字
     */
    private String getClassNameById(String classId) {
        return classesService.getById(classId).getName();
    }

    /**
     * 根据班级名字获取班级ID
     *
     * @param className 班级名字
     * @return 班级id
     */
    private String getClassIdByName(String className) {
        return classesService.getOne(new QueryWrapper<Classes>().eq("name", className)).getCid();
    }

    /**
     * 根据模板Id和学生Id来定位学生提交的报告
     *
     * @param templateId 模板Id
     * @param studentId  学生Id
     * @return 模板Id和学生Id所对应的实验报告
     */
    private Report getReportByTemplateIdAndStudentId(String templateId, String studentId) {
        QueryWrapper<Report> reportQueryWrapper = new QueryWrapper<>();
        reportQueryWrapper.select("rid", "filename", "type", "uploader", "report_template", "upload_time", "status")
                .eq("report_template", templateId)
                .eq("uploader", studentId);
        return reportService.getOne(reportQueryWrapper);
    }

    /**
     * 根据模板Id和学生Id来定位学生提交的报告
     *
     * @param templateId 模板Id
     * @param studentId  学生Id
     * @return 模板Id和学生Id所对应的实验报告
     */
    private Report getReportByTemplateIdAndStudentIdWithData(String templateId, String studentId) {
        QueryWrapper<Report> reportQueryWrapper = new QueryWrapper<>();
        reportQueryWrapper.select("rid", "filename", "type", "data", "uploader", "report_template", "upload_time")
                .eq("report_template", templateId)
                .eq("uploader", studentId);
        return reportService.getOne(reportQueryWrapper);
    }


    /**
     * 根据学生id和模板id获取学生提交的实验报告
     *
     * @param studentId  学生id
     * @param templateId 模板id
     * @return 学生提交的实验报告对象
     */
    private Report getReportByStudentIdAndTemplateId(String studentId, String templateId) {
        QueryWrapper<Report> reportQueryWrapper = new QueryWrapper<>();
        reportQueryWrapper.select("rid", "filename", "type", "uploader", "report_template", "upload_time")
                .eq("uploader", studentId)
                .eq("report_template", templateId);
        return reportService.getOne(reportQueryWrapper);
    }

    /**
     * 根据学生id获取学生姓名
     *
     * @param id 学生id
     * @return 学生id对应的学生姓名
     */
    private String getStudentNameById(String id) {
        return studentService.getById(id).getName();
    }

    /**
     * 根据教师id返回教师所授班级的Node集合
     *
     * @param teacherId 教师Id
     * @return 教师所教课程/班级的Node集合
     */
    private List<Node> getNodeList(String teacherId) {
        ArrayList<Node> nodes = new ArrayList<>();
        QueryWrapper<ClassesCourse> wrapper = new QueryWrapper<>();
        wrapper.eq("teacher_id", teacherId);
        List<ClassesCourse> list = classesCourseService.list(wrapper);
        for (ClassesCourse classesCourse : list) {
            Course course = courseService.getById(classesCourse.getCourseId());
            String courseName = course.getName();
            String courseId = course.getCourseId();
            Node node = new Node(courseId, "0", courseName, "#");
            if (!nodes.contains(node)) {
                nodes.add(node);
                QueryWrapper<ClassesCourse> courseQueryWrapper = new QueryWrapper<>();
                courseQueryWrapper.eq("teacher_id", teacherId)
                        .eq("course_id", courseId);
                List<ClassesCourse> list1 = classesCourseService.list(courseQueryWrapper);
                for (ClassesCourse classesCourse1 : list1) {
                    Classes aClass = classesService.getById(classesCourse1.getClassId());
                    String className = aClass.getName();
                    nodes.add(new Node(courseId + aClass.getCid(), courseId, className, String.format("%s@%s", aClass.getCid(), courseId)));
                }
            }
        }
        return nodes;
    }

    /**
     * 查询教师所教班级
     *
     * @return 教师所教班级的HashMap -> key:课程id@班级id, value:班级名 课程名
     */
    private HashMap<String, String> getTeacherClasses(String teacherId) {
        QueryWrapper<ClassesCourse> classesCourseQueryWrapper = new QueryWrapper<>();
        classesCourseQueryWrapper.eq("teacher_id", teacherId);
        List<ClassesCourse> classesCourses = classesCourseService.list(classesCourseQueryWrapper);
        HashMap<String, String> classesStringHashMap = new HashMap<>();
        for (ClassesCourse classesCourse : classesCourses) {
            // 教的班级的id
            String id = classesCourse.getClassId();
            // 教的课程id
            String courseIdAndClassId = String.format("%s@%s", classesCourse.getCourseId(), classesCourse.getClassId());
            String name = String.format("%s %s", classesService.getById(id).getName(),
                    courseService.getById(classesCourse.getCourseId()).getName());
            classesStringHashMap.put(name, courseIdAndClassId);
        }
        return classesStringHashMap;
    }

    /**
     * 根据教师id返回模板的分页
     *
     * @param pn        一页几条数据
     * @param teacherId 教师Id
     * @return 分页
     */
    private Page<Template> getTeacherTemplatePagination(Integer pn, String teacherId) {
        QueryWrapper<Template> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("template_id", "name", "type", "template_teacher", "class_id", "deadline").
                eq("template_teacher", teacherId);
        Page<Template> userPage = new Page<>(pn, 10);
        // 分页查询结果
        Page<Template> templates = templateService.page(userPage, queryWrapper);
        for (Template record : templates.getRecords()) {
            record.setClassId(classesService.getById(record.getClassId()).getName());
            record.setTemplateTeacher(String.format("%s(%s)", record.getTemplateTeacher(), teacherId));
        }
        return templates;
    }


    /**
     * 根据模板Id获取一个没有数据的模板对象
     *
     * @param teacherId teacher id
     * @return Template实体
     */
    private List<Template> getTemplateListByTnoCidCourseId(String teacherId, String cid, String courseId) {
        QueryWrapper<Template> wrapper = new QueryWrapper<>();
        wrapper.select("template_id", "name", "type", "template_teacher", "class_id", "course_id", "deadline")
                .eq("template_teacher", teacherId)
                .eq("course_id", courseId)
                .eq("class_id", cid)
                .orderByAsc("name");
        return templateService.list(wrapper);
    }

}
