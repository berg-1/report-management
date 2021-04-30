package com.neo.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Berg
 * @TableName template
 */
@TableName(value = "template")
@Data
public class Template implements Serializable {
    /**
     * 模板ID
     */
    private String templateId;

    /**
     * 模板名
     */
    private String name;

    /**
     * 模板类型
     */
    private String type;

    /**
     * 上传模板的老师
     */
    private String templateTeacher;

    /**
     * 写这个作业班级的id
     */
    private String classId;

    /**
     * DEADLINE
     */
    private Date deadline;

    /**
     * 数据部分
     */
    private byte[] data;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}