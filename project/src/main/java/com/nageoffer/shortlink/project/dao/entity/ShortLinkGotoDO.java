package com.nageoffer.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_link_goto")
public class ShortLinkGotoDO {

    private String gid;

    private Long id;

    private String fullShortUrl;
}
