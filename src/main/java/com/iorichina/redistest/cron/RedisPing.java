package com.iorichina.redistest.cron;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.iorichina.base.BaseCrontab;
import com.iorichina.util.JedisUtils;
import com.iorichina.util.MemcachedOperate;

public class RedisPing extends BaseCrontab {
	final Logger log = LoggerFactory.getLogger(RedisPing.class);
	@Autowired
	private JedisUtils jedisUtils;
	@Autowired
	private JedisPool jedisPool2;
	@Autowired
	private MemcachedOperate memcachedOperate;

	private String ip2;
	private int port2;
	private String redisMemcKey;

	private int maxTry = 2;
	private int _bad_trys;

	private Jedis redisClient;

	@Override
	public void init() {
		super.init();
		getRedis();
	}

	@Override
	public void oneExecute() {
		try {
			String pong = getRedis().ping();
			if (pong == null) {
				throw new Exception("ping fail");
			}
		} catch (Exception e) {
			_bad_trys++;
			log.error("Redis ping() fail: " + _bad_trys, e);
			// e.printStackTrace();

			if (_bad_trys > maxTry) {
				log.error("Redis ping() fail trigger: " + maxTry);
				getRedis(jedisPool2).slaveofNoOne();
				jedisUtils.destroy();

				// set memcache
				try {
					memcachedOperate.pushAll(redisMemcKey, ip2 + ":" + port2,
							86400);
				} catch (Exception e2) {
					e2.printStackTrace();
				}

				// close thread task
				try {
					close();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	private Jedis getRedis(JedisPool jedisPool) {
		jedisUtils = new JedisUtils(jedisPool);
		try {
			return jedisUtils.getJedis();
		} catch (Exception e) {
			log.error("getRedis(JedisPool) fail", e);
			return null;
		}
	}

	public Jedis getRedis() {
		if (redisClient != null) {
			return redisClient;
		}
		try {
			redisClient = jedisUtils.getJedis();
		} catch (Exception e) {
			log.error("getRedis() fail", e);
		}
		return redisClient;
	}

	public int getMaxTry() {
		return maxTry;
	}

	public void setMaxTry(int maxTry) {
		this.maxTry = maxTry;
	}

	public String getIp2() {
		return ip2;
	}

	public void setIp2(String ip2) {
		this.ip2 = ip2;
	}

	public int getPort2() {
		return port2;
	}

	public void setPort2(int port2) {
		this.port2 = port2;
	}

	public JedisUtils getJedisUtils() {
		return jedisUtils;
	}

	public void setJedisUtils(JedisUtils jedisUtils) {
		this.jedisUtils = jedisUtils;
	}

	public Jedis getRedisClient() {
		return redisClient;
	}

	public void setRedisClient(Jedis redisClient) {
		this.redisClient = redisClient;
	}

	public void closeRedisClient() {
		if (redisClient != null) {
			jedisUtils.returnResource(redisClient);
			redisClient = null;
		}
	}

	@Override
	public Logger logger() {
		return log;
	}

	public String getRedisMemcKey() {
		return redisMemcKey;
	}

	public void setRedisMemcKey(String redisMemcKey) {
		this.redisMemcKey = redisMemcKey;
	}

}
