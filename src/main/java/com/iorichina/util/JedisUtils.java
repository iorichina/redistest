package com.iorichina.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisUtils {
	final Logger log = LoggerFactory.getLogger(JedisUtils.class);
	@Autowired
	private JedisPoolConfig jedisPoolConfig;
	@Autowired
	private JedisPool jedisPool;
	
	public JedisUtils(){}

	public JedisUtils(String host, int port) {
		if (jedisPool != null) {
			log.info("cover jedisPool");
		}
		jedisPool = new JedisPool(jedisPoolConfig, host, port);
	}

	public JedisUtils(JedisPool pool) {
		if (jedisPool != null) {
			log.info("cover jedisPool");
		}
		jedisPool = pool;
	}

	public Jedis getJedis() throws Exception {
		Jedis jedis = null;
		if (jedisPool == null) {
			return jedis;
		}
		try {
			jedis = jedisPool.getResource();
		} catch (Exception e) {
			log.info("redis is close, restart");
			jedis = jedisPool.getResource();
		}
		return jedis;
	}

	public void returnResource(Jedis jedis) {
		if (jedis != null && jedisPool != null) {
			jedisPool.returnResource(jedis);
		}
	}

	public void destroy() {
		jedisPool.destroy();
		jedisPool = null;
	}

	public JedisPool getJedisPool() {
		return jedisPool;
	}

	public void setJedisPool(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}

	public JedisPoolConfig getJedisPoolConfig() {
		return jedisPoolConfig;
	}

	public void setJedisPoolConfig(JedisPoolConfig jedisPoolConfig) {
		this.jedisPoolConfig = jedisPoolConfig;
	}

}
