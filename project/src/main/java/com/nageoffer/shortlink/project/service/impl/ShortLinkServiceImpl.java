package com.nageoffer.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.project.common.convention.exception.ServiceException;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dao.mapper.ShortLinkMapper;
import com.nageoffer.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.nageoffer.shortlink.project.service.ShortLinkService;
import com.nageoffer.shortlink.project.util.HashUtil;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;

    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestparam) {
        ShortLinkDO shortLinkDO = BeanUtil.toBean(requestparam,ShortLinkDO.class);
        String shortUri = generateSuffix(requestparam);
        shortLinkDO.setShortUri(shortUri);
        String fullShortUrl = requestparam.getDomain() + "/" + shortUri;
        shortLinkDO.setFullShortUrl(fullShortUrl);
        shortLinkDO.setEnableStatus(0);
        try {
            baseMapper.insert(shortLinkDO);
        } catch (DuplicateKeyException ex) {
            ShortLinkDO hasShortLinkDO = lambdaQuery().eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                    .one();
            if (hasShortLinkDO != null) {
                throw new ServiceException("短链接入库重复");
            }
        }
        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
        ShortLinkCreateRespDTO shortLinkCreateRespDTO = new ShortLinkCreateRespDTO();
        shortLinkCreateRespDTO.setFullShortUrl(fullShortUrl);
        shortLinkCreateRespDTO.setGid(requestparam.getGid());
        shortLinkCreateRespDTO.setOriginUrl(requestparam.getOriginUrl());
        return shortLinkCreateRespDTO;
    }

    @Override
    public IPage<ShortLinkPageRespDTO> page(ShortLinkPageReqDTO requestparam) {
        IPage<ShortLinkDO> page = lambdaQuery().eq(ShortLinkDO::getGid, requestparam.getGid())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0)
                .orderByDesc(ShortLinkDO::getCreateTime)
                .page(requestparam);
        return page.convert(shortLinkDO -> BeanUtil.toBean(shortLinkDO,ShortLinkPageRespDTO.class));
    }

    private String generateSuffix(ShortLinkCreateReqDTO requestparam) {
        String originUrl = requestparam.getOriginUrl();
        int count = 0;
        String shortUri;
        while (true) {
            if (count > 10) {
                throw new ServiceException("短链接生成频繁");
            }
            originUrl += System.currentTimeMillis();
            shortUri = HashUtil.hashToBase62(originUrl);
            if (!shortUriCreateCachePenetrationBloomFilter.contains(requestparam.getDomain()+"/"+shortUri)) {

                break;
            }
            count++;
        }
        return shortUri;
    }
}
