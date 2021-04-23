package com.neo.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import lombok.Data;

/**
 * @author Berg
 */
@TableName(value = "classes")
@Data
public class Classes implements Serializable {
    /**
     *
     */
    @TableId
    private String cid;

    /**
     *
     */
    private String name;

    /**
     *
     */
    private String teacherId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}