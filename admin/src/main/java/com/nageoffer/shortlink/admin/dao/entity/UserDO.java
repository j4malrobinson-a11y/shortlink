package com.nageoffer.shortlink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @description t_user
 * @author zhengkai.blog.csdn.net
 * @date 2026-03-12
 */
@Data
@TableName("t_user")
public class UserDO extends BaseDO{


    @TableId(type = IdType.AUTO)
    /**
    * ID
    */
    private Long id;

    /**
    * 用户名
    */
    private String username;

    /**
    * 密码
    */
    private String password;

    /**
    * 真实姓名
    */
    private String realName;

    /**
    * 手机号
    */
    private String phone;

    /**
    * 邮箱
    */
    private String mail;

    /**
    * 注销时间戳
    */
    private Long deletionTime;




}