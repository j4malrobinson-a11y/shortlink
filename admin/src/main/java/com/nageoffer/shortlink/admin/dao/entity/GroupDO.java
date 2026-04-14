package com.nageoffer.shortlink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @description t_group
 * @author BEJSON.com
 * @date 2026-04-13
 */
@Data
@TableName("t_group")
public class GroupDO extends BaseDO{

    /**
    * ID
    */
    private Long id;

    /**
    * 分组标识
    */
    private String gid;

    /**
    * 分组名称
    */
    private String name;

    /**
    * 创建分组用户名
    */
    private String username;

    /**
    * 分组排序
    */
    private Integer sortOrder;



}