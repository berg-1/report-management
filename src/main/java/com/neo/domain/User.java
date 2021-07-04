package com.neo.domain;

import lombok.Data;
import lombok.ToString;

/**
 * 用户登录临时类
 */
@Data
@ToString
public class User {
    String username;
    String password;
}
