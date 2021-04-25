package com.neo.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Berg
 */
@TableName(value = "report")
@Data
@AllArgsConstructor
public class Report implements Serializable {
    /**
     * 实验报告UUID
     */
    @TableId
    private String rid;

    /**
     * 文件名
     */
    private String filename;

    /**
     * 文件类型
     */
    private String type;

    /**
     * 实验报告上传者-学生id
     */
    private String uploader;

    /**
     * 实验报告对应的模板
     */
    private String reportTemplate;

    /**
     * 上传时间
     */
    private Date uploadTime;

    /**
     * 文件数据
     */
    private byte[] data;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}