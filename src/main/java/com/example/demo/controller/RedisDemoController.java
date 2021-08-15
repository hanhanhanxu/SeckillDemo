package com.example.demo.controller;

import com.example.demo.redis.JedisPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.util.List;
import java.util.Random;

import static com.example.demo.script.SecKill_redisByScript.secKillScript;

/**
 * @author: HanXu
 * on 2021/8/11
 * Class description: 秒杀案例
 */
@Slf4j
@RestController
public class RedisDemoController {

    @Autowired
    @Qualifier(value = "redisPoolFactory")
    private JedisPool jedisPool;

    // 储存剩余商品数量
    private static final String spKey = "spKey:";
    // 储存已经秒杀到的用户id
    private static final String userKey = "userKey:";

    @PostMapping("doseckill")
    public boolean test(String spId) {
        String userId = getRundom(6);
        log.info("-----入参商品ip:{}, 用户id:{}", spId, userId);
        //开始秒杀
        boolean result = doseckill3(spId, userId);
        //返回秒杀结果
        return result;
    }

    @PostMapping("doseckill_lua")
    public boolean test2(String spId) {
        String userId = getRundom(6);
        log.info("-----入参商品ip:{}, 用户id:{}", spId, userId);
        //开始秒杀
        boolean result = doSecKill_lua(spId, userId);
        //返回秒杀结果
        return result;
    }

    public String getRundom(int num) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < num; i++) {
            int n = random.nextInt(10);
            sb.append(n);
        }
        return sb.toString();
    }

    /**
     * 头晕了，有时候正常，有时候有超卖，库存剩下-1
     * @param spId
     * @param userId
     * @return
     */
    public boolean doseckill3(String spId, String userId) {
        JedisPool jedisPool = JedisPoolUtil.getJedisPoolInstance();
        Jedis jedis = jedisPool.getResource();
        try {
            // 前置检查
            String kcCount = jedis.get(spKey + spId);
            if (StringUtils.isBlank(kcCount)) {
                log.error("秒杀还未开始");
                return false;
            }
            if (Integer.parseInt(kcCount) <= 0) {
                log.error("秒杀已经结束了");
                return false;
            }
            Boolean sismember = jedis.sismember(userKey + spId, userId);
            if (sismember) {
                log.info("已经秒杀到了，不能在买了");
                return false;
            }

            // 秒杀业务处理
            String watch = jedis.watch(spKey + spId);
            log.info("watch:{}", watch);
            Transaction multi = jedis.multi();
            multi.decr(spKey + spId);
            multi.sadd(userKey + spId, userId);
            List<Object> result = multi.exec();
            if (result == null || result.size() == 0) {
                log.error("秒杀失败，请稍等再来");
                return false;
            }

            log.info("userId:{} 秒杀成功", userId);
        } catch (Exception e) {
            log.error("抛出异常:{}", e);
        } finally {
            JedisPoolUtil.release(jedisPool, jedis);
        }
        return true;
    }


    /**
     * 解决超卖和库存剩余问题
     * @param spId
     * @param userId
     * @return
     */
    public boolean doSecKill_lua(String spId, String userId) {
        JedisPool jedisPool =  JedisPoolUtil.getJedisPoolInstance();
        Jedis jedis = jedisPool.getResource();

        String sha1 =  jedis.scriptLoad(secKillScript);
        Object result = jedis.evalsha(sha1, 2, userId, spId);

        String reString = String.valueOf(result);
        if ("0".equals( reString )) {
            log.error("秒杀已经结束了");
        } else if ("1".equals( reString )) {
            log.info("userId:{} 秒杀成功", userId);
        } else if ("2".equals( reString )) {
            log.info("已经秒杀到了，不能在买了");
        } else {
            log.info("秒杀异常");
        }
        JedisPoolUtil.release(jedisPool, jedis);
        return true;
    }
}
