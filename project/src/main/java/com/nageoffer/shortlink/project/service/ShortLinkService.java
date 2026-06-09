package com.nageoffer.shortlink.project.service;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkUpdateReq;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageRespDTO;

import java.io.IOException;
import java.util.List;

public interface ShortLinkService {


    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestparam);

    IPage<ShortLinkPageRespDTO> page(ShortLinkPageReqDTO requestparam);

    List<ShortLinkGroupCountQueryRespDTO> countByGids(List<String> gids);

    void updateShortLink(ShortLinkUpdateReq requestparam);

    void restoreUrl(String shortUri, HttpServletRequest request, HttpServletResponse response) throws IOException;
}
