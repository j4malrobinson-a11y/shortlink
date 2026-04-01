package com.nageoffer.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.admin.common.comstant.RedisCacheConstant;
import com.nageoffer.shortlink.admin.common.convention.exception.ClientException;
import com.nageoffer.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.nageoffer.shortlink.admin.dao.entity.UserDO;
import com.nageoffer.shortlink.admin.dao.mapper.UserMapper;
import com.nageoffer.shortlink.admin.dto.resp.UserRegisterReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserRespDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserUpdateReqDTO;
import com.nageoffer.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import static com.nageoffer.shortlink.admin.common.enums.UserErrorCodeEnum.USER_NAME_EXIT;
import static com.nageoffer.shortlink.admin.common.enums.UserErrorCodeEnum.USER_SAVE_ERROR;

/**
 * 用户接口实现层
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService  {

    private final UserMapper userMapper;

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;

    private final RedissonClient redissonClient;

    @Override
    public UserRespDTO getUserByUserName(String username) {
        UserDO userDO = lambdaQuery()
                .eq(UserDO::getUsername, username)
                .one();
        if (userDO==null){
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
        }
        UserRespDTO result = new UserRespDTO();
        BeanUtils.copyProperties(userDO,result);
        return result;
    }

    @Override
    public void register(UserRegisterReqDTO requestParam) {
        // 先检查布隆过滤器：如果用户名可能存在，需要加锁进一步检查数据库
        if(userRegisterCachePenetrationBloomFilter.contains(requestParam.getUsername())){
            // 用户名可能已存在，需要获取分布式锁进行精确检查
            RLock lock = redissonClient.getLock(RedisCacheConstant.LOOK_USER_REGISTER_KEY+requestParam.getUsername());
            try {
                if(lock.tryLock()){
                    // 在锁内检查数据库，确保用户名不存在
                    Long count = lambdaQuery()
                        .eq(UserDO::getUsername, requestParam.getUsername())
                        .count();
                    if(count > 0){
                        throw new ClientException(USER_NAME_EXIT);
                    }

                    // 插入新用户
                    int inserted = userMapper.insert(BeanUtil.toBean(requestParam, UserDO.class));
                    if(inserted <1){
                        throw new ClientException(USER_SAVE_ERROR);
                    }
                    userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
                } else {
                    // 获取锁失败，说明其他线程正在注册相同的用户名
                    throw new ClientException(USER_NAME_EXIT);
                }
            } finally {
                lock.unlock();
            }
        } else {
            // 布隆过滤器确认用户名肯定不存在，可以直接注册
            int inserted = userMapper.insert(BeanUtil.toBean(requestParam, UserDO.class));
            if(inserted <1){
                throw new ClientException(USER_SAVE_ERROR);
            }
            userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
        }
    }

    @Override
    public void update(UserUpdateReqDTO requestRaram) {
        LambdaQueryWrapper<UserDO> eq = lambdaQuery().getWrapper().eq(UserDO::getUsername, requestRaram.getUsername());
        userMapper.update(BeanUtil.toBean(requestRaram,UserDO.class),eq);
    }
}
