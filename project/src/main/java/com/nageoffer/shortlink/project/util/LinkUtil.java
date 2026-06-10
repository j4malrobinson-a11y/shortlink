package com.nageoffer.shortlink.project.util;

import cn.hutool.core.util.RandomUtil;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class LinkUtil {

    /**
     * 最大缓存时间：7天
     */
    private static final long MAX_CACHE_MILLIS = TimeUnit.DAYS.toMillis(7);

    /**
     * 随机增加 1~24 小时，防止缓存雪崩
     */
    private static final long RANDOM_CACHE_MILLIS =
            TimeUnit.HOURS.toMillis(RandomUtil.randomInt(1, 25));

    /**
     * 根据短链接有效期计算 Redis 缓存时间
     *
     * @param validDate 短链接过期时间，null 表示永久有效
     * @return Redis 缓存时间（毫秒）
     */
    public static long getCacheValidTime(Date validDate) {

        // 永久有效
        if (validDate == null) {
            return MAX_CACHE_MILLIS + RANDOM_CACHE_MILLIS;
        }

        // 剩余有效时间
        long remainTime = validDate.getTime() - System.currentTimeMillis();

        // 已过期
        if (remainTime <= 0) {
            return 0;
        }

        // 缓存时间取「剩余时间」和「7天」中的较小值
        return Math.min(remainTime, MAX_CACHE_MILLIS);
    }
}