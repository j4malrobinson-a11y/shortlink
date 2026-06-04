package com.nageoffer.shortlink.admin.remote.dto.resp;

import lombok.Data;

@Data
public class ShortLinkGroupCountQueryRespDTO {
    /**
     * 分组标识
     */
    private String gid;

    private Integer shortLinkGroupCount;

}
