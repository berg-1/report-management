package com.neo.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import lombok.Data;

/**
 * @author Berg
 * @TableName classes_course
 */
@TableName(value = "classes_course")
@Data
public class ClassesCourse implements Serializable {
    /**
     * 学生班级ID
     */
    @TableId
    private String classId;

    /**
     * 上课ID
     */
    private String courseId;

    /**
     * 教师ID
     */
    private String teacherId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}