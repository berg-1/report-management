package com.neo.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import lombok.Data;

/**
 * @author Berg
 */
@TableName(value = "student")
@Data
public class Student implements Serializable {
    /**
     *
     */
    @TableId
    private String sno;

    /**
     *
     */
    private String name;

    /**
     *
     */
    private Integer password;

    /**
     *
     */
    private String classId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}