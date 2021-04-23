package com.neo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import lombok.Data;

/**
 * @author Berg
 */
@TableName(value = "comments")
@Data
public class Comments implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Integer commentId;

    /**
     *
     */
    private String commentReport;

    /**
     *
     */
    private String comment;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}