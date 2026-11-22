package com.youkeda.hashopping.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Collections;
import java.util.Locale;
import java.util.regex.Pattern;

/** 通过 Redis 保存连续登录失败次数，并按用户名执行短期锁定。 */
@Component
public class LoginAttemptGuard {
    private static final int MAX_FAILURES = 5;
    private static final long WINDOW_SECONDS = 30 * 60;
    private static final Pattern MARKS = Pattern.compile("\\p{M}+");

    // 首次失败启动计数窗口，第五次失败重新开始完整的锁定时间。
    private static final DefaultRedisScript<Long> RECORD_FAILURE = new DefaultRedisScript<>(
            "local count = tonumber(redis.call('GET', KEYS[1]) or '0'); " +
            "local limit = tonumber(ARGV[1]); " +
            "if count >= limit then return count end; " +
            "count = redis.call('INCR', KEYS[1]); " +
            "if count == 1 or count == limit then redis.call('EXPIRE', KEYS[1], ARGV[2]) end; " +
            "return count", Long.class);

    // 成功请求到达时若另一个请求已触发锁定，则保留锁定。
    private static final DefaultRedisScript<Long> CLEAR_FAILURES = new DefaultRedisScript<>(
            "local count = tonumber(redis.call('GET', KEYS[1]) or '0'); " +
            "if count >= tonumber(ARGV[1]) then return 0 end; " +
            "redis.call('DEL', KEYS[1]); return 1", Long.class);

    private final StringRedisTemplate redis;

    /** 使用字符串模板读写登录失败次数。 */
    public LoginAttemptGuard(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /** 查询当前用户名是否已达到失败次数上限。 */
    public boolean isLocked(String userName) {
        String count = redis.opsForValue().get(key(userName));
        return count != null && Long.parseLong(count) >= MAX_FAILURES;
    }

    /** 原子增加失败次数，返回本次失败后是否已锁定。 */
    public boolean recordFailure(String userName) {
        Long count = redis.execute(RECORD_FAILURE, Collections.singletonList(key(userName)),
                Integer.toString(MAX_FAILURES), Long.toString(WINDOW_SECONDS));
        return count != null && count >= MAX_FAILURES;
    }

    /** 登录成功时清除未达上限的失败记录，已锁定则返回 false。 */
    public boolean clearAfterSuccess(String userName) {
        Long cleared = redis.execute(CLEAR_FAILURES, Collections.singletonList(key(userName)),
                Integer.toString(MAX_FAILURES));
        return Long.valueOf(1).equals(cleared);
    }

    /** 生成失败计数键，将常见的大小写、重音和空格变体归到同一用户名。 */
    private String key(String userName) {
        String normalized = Normalizer.normalize(userName.strip().toLowerCase(Locale.ROOT), Normalizer.Form.NFKD);
        return "login:password-error:" + MARKS.matcher(normalized).replaceAll("");
    }
}
