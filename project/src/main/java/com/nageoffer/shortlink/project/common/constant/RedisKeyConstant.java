package com.nageoffer.shortlink.project.common.constant;

public class RedisKeyConstant {

    public static final String GOTO_SHORT_LINK_KEY = "shortlink_goto_%s";

    public static final String LOCK_GOTO_SHORT_LINK_KEY = "shortlink_lock_goto_%s";

    /**
     * 短链接空值跳转前缀 Key
     */
    public static final String GOTO_IS_NULL_SHORT_LINK_KEY = "shortlink_is_null_goto_%s";
}
