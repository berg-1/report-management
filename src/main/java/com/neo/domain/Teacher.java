package com.neo.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import lombok.Data;

/**
 * @author Berg
 */
@TableName(value = "teacher")
@Data
public class Teacher implements Serializable {
    /**
     *
     */
    @TableId
    private String tno;

    /**
     *
     */
    private String tname;

    /**
     *
     */
    private String password;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}