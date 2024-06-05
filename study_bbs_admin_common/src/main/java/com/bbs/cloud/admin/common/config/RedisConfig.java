package com.bbs.cloud.admin.common.config;

import com.bbs.cloud.admin.common.util.RedisPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * redis的配置类：连接池生成redis实例，从而实现操作redis的目的
 */
@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port:0}")
    private int port;

    @Value("${spring.redis.database:0}")
    private int database;
    @Value("${spring.redis.password}")
    private String password;

    @Bean
    public RedisPool getRedisPool() {
        if(host.equals("disabled")) {
            return null;
        }
        RedisPool redisPool =new RedisPool();
        redisPool.initPool(host, port, database, password);
        return redisPool;
    }

}
