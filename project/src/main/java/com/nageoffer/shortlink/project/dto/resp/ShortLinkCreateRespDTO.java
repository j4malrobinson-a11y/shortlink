package com.nageoffer.shortlink.project.dto.resp;

import lombok.Data;

@Data
public class ShortLinkCreateRespDTO {

    /**
     * 完整短链接
     */
    private String fullShortUrl;


    /**
     * 原始链接
     */
    private String originUrl;

    /**
     * 分组ID
     */
    private String gid;
}