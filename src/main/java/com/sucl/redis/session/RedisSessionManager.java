package com.sucl.redis.session;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * CustomRedisSessionConfiguration控制了过期时间
 * @author sucl
 * @date 2019/5/23
 */
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 50*60,redisNamespace = "redis")
public class RedisSessionManager {
}
