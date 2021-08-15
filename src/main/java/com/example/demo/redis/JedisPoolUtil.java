package com.example.demo.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author: HanXu
 * on 2021/8/12
 * Class description: 单例。JedisPoolUtil和JedisPoolConfiguration选择一个就好
 */
public class JedisPoolUtil {
	private static volatile JedisPool jedisPool = null;

	private JedisPoolUtil() {
	}

	public static JedisPool getJedisPoolInstance() {
		if (null == jedisPool) {
			synchronized (JedisPoolUtil.class) {
				if (null == jedisPool) {
					JedisPoolConfig poolConfig = new JedisPoolConfig();
					poolConfig.setMaxTotal(32);
					poolConfig.setMaxIdle(32);
					poolConfig.setMinIdle(32);
					poolConfig.setBlockWhenExhausted(true);
					poolConfig.setMaxWaitMillis(1*1000);
					// ping  PONG
					poolConfig.setTestOnBorrow(true);
					poolConfig.setTestOnReturn(true);

					jedisPool = new JedisPool(poolConfig, "127.0.0.1", 6379, 60000, "hanxu");
				}
			}
		}
		return jedisPool;
	}

	// jedis使用后要手动关闭资源，因为jedis底层连接redis的是socket,占用资源，必须手动关闭。
	public static void release(JedisPool jedisPool, Jedis jedis) {
		if (null != jedis) {
			//jedisPool.returnResource(jedis);
			jedis.close();
		}
	}

}
