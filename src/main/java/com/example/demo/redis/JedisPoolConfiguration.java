package com.example.demo.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author: HanXu
 * on 2021/8/12
 * Class description: 注入。JedisPoolUtil和JedisPoolConfiguration选择一个就好
 */
@Slf4j
@Component
public class JedisPoolConfiguration {

    @Value("${redis.pool.host:127.0.0.1}")
    private String host;
    @Value("${redis.pool.port:6379}")
    private int port;
    @Value("${redis.pool.password:hanxu}")
    private String password;
    @Value("${redis.pool.timeout:2000}")
    private int timeout;
    @Value("${redis.pool.max-active:32}")
    private int maxActive;
    @Value("${redis.pool.max-idle:32}")
    private int maxIdle;
    @Value("${redis.pool.min-idle:32}")
    private int minIdle;
    @Value("${redis.pool.max-wait:1000}")
    private int maxWait;

    @Bean
    public JedisPool redisPoolFactory() {
        try {
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            jedisPoolConfig.setMaxIdle(maxIdle);
            jedisPoolConfig.setMaxWaitMillis(maxWait);
            jedisPoolConfig.setMaxTotal(maxActive);
            jedisPoolConfig.setMinIdle(minIdle);
            JedisPool jedisPool = new JedisPool(jedisPoolConfig, host, port, timeout, password);
            log.info("初始化JedisPool成功!地址: " + host + ":" + port);
            return jedisPool;
        } catch (Exception e) {
            log.error("初始化JedisPool异常:" + e.getMessage());
        }
        return null;
    }
}
