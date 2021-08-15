package com.example.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author: HanXu
 * on 2021/8/15
 * Class description: redis实现分布式锁
 */
@Slf4j
@RestController
public class RedisLockController {

    @Autowired
    private RedisTemplate redisTemplate;

    // set num "0"
    // ab -n 1000 -c 100 http://localhost:8080/testLock

    @GetMapping("testLock")
    public void testLock() {
        // 尝试获取锁
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 10, TimeUnit.SECONDS);
        if (lock) {
            // 业务操作
            Object num = redisTemplate.opsForValue().get("num");
            if (num == null) {
                return;
            }
            int n = Integer.parseInt(num + "");
            redisTemplate.opsForValue().set("num", String.valueOf(++n));

            // 释放锁 删除操作非原子性 判断和实际的操作不原子会导致删除到其他机器的锁
            /*if (uuid.equals(redisTemplate.opsForValue().get("lock"))) {
                redisTemplate.delete("lock");
            }*/

            // 释放锁 使用lua脚本将判断和执行做成原子操作
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(script);
            redisScript.setResultType(Long.class);
            redisTemplate.execute(redisScript, Arrays.asList("lock"), uuid);
        } else {
            // 获取不到就稍等再获取
            try {
                Thread.sleep(1000);
                testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
