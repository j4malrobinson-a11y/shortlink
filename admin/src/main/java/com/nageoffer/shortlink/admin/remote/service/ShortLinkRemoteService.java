package com.nageoffer.shortlink.admin.remote.service;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ShortLinkRemoteService {

    default Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {

        // 1. 组装参数
        Map<String, Object> requestMap = new HashMap<>();

        requestMap.put("gid", requestParam.getGid());
        requestMap.put("current", requestParam.getCurrent());
        requestMap.put("size", requestParam.getSize());

        // 2. 发HTTP请求
        String resultStr = HttpUtil.get(
                "http://127.0.0.1:8001/api/shortlink/v1/page",
                requestMap
        );

        // 3. JSON转对象
        return JSON.parseObject(
                resultStr,
                new TypeReference<>() {}
        );
    }

    default Result<ShortLinkCreateRespDTO> createShortLink(ShortLinkCreateReqDTO requestparam){
        String resultStr = HttpUtil.post(
                "http://127.0.0.1:8001/api/shortlink/v1/create",
                JSON.toJSONString(requestparam)
        );

        return JSON.parseObject(resultStr, new TypeReference<Result<ShortLinkCreateRespDTO>>() {
        });
    }

    default Result<List<ShortLinkGroupCountQueryRespDTO>> listGroupShortLinkCount(List<String> requestParam) {

        // 1. 组装参数
        Map<String, Object> requestMap = new HashMap<>();

        requestMap.put("gids", requestParam);

        // 2. 发HTTP请求
        String resultStr = HttpUtil.get(
                "http://127.0.0.1:8001/api/shortlink/v1/count",
                requestMap
        );

        // 3. JSON转对象
        return JSON.parseObject(
                resultStr,
                new TypeReference<>() {}
        );
    }
}