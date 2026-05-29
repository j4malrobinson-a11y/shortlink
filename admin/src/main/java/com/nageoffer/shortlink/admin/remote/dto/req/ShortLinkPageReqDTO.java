package com.nageoffer.shortlink.admin.remote.dto.req;

import lombok.Data;

@Data
public class ShortLinkPageReqDTO {

    private String gid;

    private Long current;

    private Long size;
}