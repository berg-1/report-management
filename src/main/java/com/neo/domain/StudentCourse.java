package com.neo.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Berg
 * @TableName student_course
 */
@TableName(value = "student_course")
@Data
public class StudentCourse implements Serializable {
    /**
     * 学生id
     */
    private String studentId;

    /**
     * 选课id
     */
    private String courseId;

    /**
     * 课程老师名称
     */
    private String teacherId;

    /**
     * 该科成绩
     */
    private Integer grade;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}