package com.nageoffer.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dao.mapper.ShortLinkMapper;
import com.nageoffer.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.project.service.ShortLinkService;
import com.nageoffer.shortlink.project.util.HashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {



    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestparam) {
        ShortLinkDO shortLinkDO = BeanUtil.toBean(requestparam,ShortLinkDO.class);
        shortLinkDO.setShortUri(generateSuffix(requestparam));
        shortLinkDO.setFullShortUrl(requestparam.getDomain()+"/"+generateSuffix(requestparam));
        shortLinkDO.setEnableStatus(0);
        baseMapper.insert(shortLinkDO);
        ShortLinkCreateRespDTO shortLinkCreateRespDTO = new ShortLinkCreateRespDTO();
        shortLinkCreateRespDTO.setFullShortUrl(requestparam.getDomain()+"/"+generateSuffix(requestparam));
        shortLinkCreateRespDTO.setGid(requestparam.getGid());
        shortLinkCreateRespDTO.setOriginUrl(requestparam.getOriginUrl());
        return shortLinkCreateRespDTO;
    }

    private String generateSuffix(ShortLinkCreateReqDTO requestparam) {
        String originUrl = requestparam.getOriginUrl();
        return HashUtil.hashToBase62(originUrl);
    }
}
