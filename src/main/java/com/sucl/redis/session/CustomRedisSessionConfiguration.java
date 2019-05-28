package com.sucl.redis.session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.session.SessionProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;
import org.springframework.session.data.redis.config.annotation.web.http.RedisHttpSessionConfiguration;

/**
 * 过期时间交由 server.session.timeout 控制
 * @author sucl
 * @date 2019/5/24
 */
@Configuration
@EnableConfigurationProperties(SessionProperties.class)
//@AutoConfigureAfter(RedisHttpSessionConfiguration.class)
public class CustomRedisSessionConfiguration extends RedisHttpSessionConfiguration {
    private SessionProperties sessionProperties;

    @Autowired
    public void customize(SessionProperties sessionProperties, RedisOperationsSessionRepository sessionRepository) {
        this.sessionProperties = sessionProperties;
        Integer timeout = this.sessionProperties.getTimeout();
        if (timeout != null) {
//            setMaxInactiveIntervalInSeconds(timeout);
            sessionRepository.setDefaultMaxInactiveInterval(timeout);
        }
    }

}
