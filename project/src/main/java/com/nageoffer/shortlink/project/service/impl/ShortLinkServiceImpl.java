package com.nageoffer.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.project.common.constant.RedisKeyConstant;
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
import com.nageoffer.shortlink.project.util.LinkUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jodd.util.StringUtil;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;

    private final ShortLinkGotoMapper shortLinkGotoMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private final RedissonClient redissonClient;

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
        stringRedisTemplate.opsForValue().set(String.format(RedisKeyConstant.LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl),
                requestparam.getOriginUrl(),
                LinkUtil.getCacheValidTime(requestparam.getValidDate()),
                TimeUnit.MILLISECONDS);
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
    /**
     * 短链接跳转
     *
     * @param shortUri 短链接后缀
     * @param request 请求对象
     * @param response 响应对象
     */
    public void restoreUrl(String shortUri, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 拼接完整短链接，例如 nurl.ink/abc123
        String fullShortUrl = request.getServerName() + "/" + shortUri;
        // ==================== 一级查询：Redis缓存 ====================
        // 从 Redis 中获取对应的原始链接
        String originalLink = stringRedisTemplate.opsForValue()
                .get(String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, fullShortUrl));
        // 缓存命中，直接重定向
        if (StringUtil.isNotBlank(originalLink)) {
            response.sendRedirect(originalLink);
            return;
        }
        boolean contains = shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl);
        if (!contains) {
            return;
        }
        // ==================== 二级查询：加分布式锁防止缓存击穿 ====================
        // 获取 Redisson 分布式锁，同一个短链接只允许一个线程查询数据库
        RLock lock = redissonClient.getLock(
                String.format(RedisKeyConstant.LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        // 加锁
        lock.lock();
        try {
            String isNUll = stringRedisTemplate.opsForValue().get(String.format(RedisKeyConstant.GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
            if (StringUtil.isNotBlank(isNUll)) {
                return;
            }
            // ===== 双重检查 =====
            // 防止当前线程等待锁期间，其他线程已经将数据写入缓存
            originalLink = stringRedisTemplate.opsForValue()
                    .get(String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, fullShortUrl));
            // 如果已经存在缓存，则直接重定向
            if (StringUtil.isNotBlank(originalLink)) {
                response.sendRedirect(originalLink);
                return;
            }
            // ==================== 查询跳转表 ====================
            // 根据完整短链接查询 gid
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(
                    new LambdaQueryWrapper<ShortLinkGotoDO>()
                            .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl)
            );
            // 数据不存在，说明短链接不存在
            if (shortLinkGotoDO == null) {
                stringRedisTemplate.opsForValue().set(String.format(RedisKeyConstant.GOTO_IS_NULL_SHORT_LINK_KEY,fullShortUrl),"-",30, TimeUnit.MINUTES );
                return;
            }
            // ==================== 查询短链接主表 ====================
            // 根据 gid 和完整短链接查询原始链接
            // 并判断：
            // 1. del_flag = 0（未删除）
            // 2. enable_status = 0（启用状态）
            ShortLinkDO shortLinkDO = lambdaQuery()
                    .eq(ShortLinkDO::getGid, shortLinkGotoDO.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .one();
            // 查询成功
            if (shortLinkDO != null) {
                // 获取原始长链接
                originalLink = shortLinkDO.getOriginUrl();
                stringRedisTemplate.opsForValue().set(String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY,fullShortUrl),originalLink);
                // 重定向到原始链接
                response.sendRedirect(originalLink);
            }
        } finally {
            // 释放分布式锁
            lock.unlock();
        }

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
