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
public class Student implements Serializable, Comparable<Student> {
    /**
     * 学号
     */
    @TableId
    private String sno;

    /**
     * 姓名
     */
    private String name;

    /**
     * 密码
     */
    private String password;

    /**
     * 所在班级
     */
    private String classId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public int compareTo(Student o) {
        return sno.compareTo(o.getSno());
    }
}