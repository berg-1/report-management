package com.neo.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Berg
 * @TableName course
 */
@TableName(value = "course")
@Data
public class Course implements Serializable {
    /**
     * 课程id
     */
    @TableId
    private String courseId;

    /**
     * 课程名称
     */
    private String name;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}