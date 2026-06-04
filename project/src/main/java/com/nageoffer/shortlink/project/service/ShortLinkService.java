package com.nageoffer.shortlink.project.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageRespDTO;

import java.util.List;

public interface ShortLinkService {


    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestparam);

    IPage<ShortLinkPageRespDTO> page(ShortLinkPageReqDTO requestparam);

    List<ShortLinkGroupCountQueryRespDTO> countByGids(List<String> gids);
}
