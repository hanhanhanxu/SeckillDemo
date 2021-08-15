package com.example.demo.script;

import com.example.demo.redis.JedisPoolUtil;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;

public class SecKill_redisByScript {

	private static final  org.slf4j.Logger logger =LoggerFactory.getLogger(SecKill_redisByScript.class) ;

	public static void main(String[] args) throws IOException {
		JedisPool jedispool =  JedisPoolUtil.getJedisPoolInstance();
		Jedis jedis=jedispool.getResource();
		System.out.println(jedis.ping());
		doSecKill("23423434","10101");
	}

	public static String secKillScript ="local userid=KEYS[1];\r\n" +
			"local prodid=KEYS[2];\r\n" +
			"local qtkey='spKey:'..prodid;\r\n" +
			"local usersKey='userKey:'..prodid;\r\n" +
			"local userExists=redis.call(\"sismember\",usersKey,userid);\r\n" +
			"if tonumber(userExists)==1 then \r\n" +
			"   return 2;\r\n" +
			"end\r\n" +
			"local num= redis.call(\"get\" ,qtkey);\r\n" +
			"if tonumber(num)<=0 then \r\n" +
			"   return 0;\r\n" +
			"else \r\n" +
			"   redis.call(\"decr\",qtkey);\r\n" +
			"   redis.call(\"sadd\",usersKey,userid);\r\n" +
			"end\r\n" +
			"return 1" ;

	static String secKillScript2 =
			"local userExists=redis.call(\"sismember\",\"{sk}:0101:usr\",userid);\r\n" +
			" return 1";

	public static boolean doSecKill(String uid,String prodid) throws IOException {
		JedisPool jedispool =  JedisPoolUtil.getJedisPoolInstance();
		Jedis jedis=jedispool.getResource();

		String sha1=  jedis.scriptLoad(secKillScript);
		Object result= jedis.evalsha(sha1, 2, uid,prodid);

		  String reString=String.valueOf(result);
		if ("0".equals( reString )  ) {
			System.err.println("已抢空！！");
		}else if("1".equals( reString )  )  {
			System.out.println("抢购成功！！！！");
		}else if("2".equals( reString )  )  {
			System.err.println("该用户已抢过！！");
		}else{
			System.err.println("抢购异常！！");
		}
		jedis.close();
		return true;
	}
}
