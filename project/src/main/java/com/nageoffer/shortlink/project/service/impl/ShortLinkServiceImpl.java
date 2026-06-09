package com.nageoffer.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.project.common.convention.exception.ServiceException;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkGotoDO;
import com.nageoffer.shortlink.project.dao.mapper.ShortLinkGotoMapper;
import com.nageoffer.shortlink.project.dao.mapper.ShortLinkMapper;
import com.nageoffer.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkUpdateReq;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.nageoffer.shortlink.project.service.ShortLinkService;
import com.nageoffer.shortlink.project.util.HashUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;

    private final ShortLinkGotoMapper shortLinkGotoMapper;

    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestparam) {
        ShortLinkDO shortLinkDO = BeanUtil.toBean(requestparam,ShortLinkDO.class);
        String shortUri = generateSuffix(requestparam);
        shortLinkDO.setShortUri(shortUri);
        String fullShortUrl = requestparam.getDomain() + "/" + shortUri;
        shortLinkDO.setFullShortUrl(fullShortUrl);
        shortLinkDO.setEnableStatus(0);
        ShortLinkGotoDO shortLinkGotoDO = new ShortLinkGotoDO();
        shortLinkGotoDO.setFullShortUrl(fullShortUrl);
        shortLinkGotoDO.setGid(requestparam.getGid());
        try {
            baseMapper.insert(shortLinkDO);
            shortLinkGotoMapper.insert(shortLinkGotoDO);
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

    @Override
    public List<ShortLinkGroupCountQueryRespDTO> countByGids(List<String> gids) {
        return gids.stream()
                .map(gid -> {
                    ShortLinkGroupCountQueryRespDTO result = new ShortLinkGroupCountQueryRespDTO();
                    result.setGid(gid);
                    result.setShortLinkGroupCount(Math.toIntExact(
                            lambdaQuery()
                                    .eq(ShortLinkDO::getGid, gid)
                                    .eq(ShortLinkDO::getDelFlag, 0)
                                    .count()
                    ));
                    return result;
                })
                .toList();
    }

    @Override
    public void updateShortLink(ShortLinkUpdateReq requestparam) {

        //todo
//        lambdaUpdate().eq(ShortLinkDO::getFullShortUrl,requestparam.getFullShortUrl())
//                .eq(ShortLinkDO::getGid,requestparam.getOriginGid())
//                .eq(ShortLinkDO::getDelFlag,0)
//                .eq(ShortLinkDO::getEnableStatus,0)
//                .set()

    }

    @Override
    public void restoreUrl(String shortUri, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String fullShortUrl = request.getServerName() +"/"+ shortUri;

        ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(
                new LambdaQueryWrapper<ShortLinkGotoDO>()
                        .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl)
        );
        if (shortLinkGotoDO == null) {
            return ;
        }
        ShortLinkDO shortLinkDO = lambdaQuery().eq(ShortLinkDO::getGid, shortLinkGotoDO.getGid())
                .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                .eq(ShortLinkDO::getDelFlag,0)
                .eq(ShortLinkDO::getEnableStatus,0)
                .one();
        if (shortLinkDO != null) {
            response.sendRedirect(shortLinkDO.getOriginUrl());
        }

        shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl);


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
