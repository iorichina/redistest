package com.iorichina.redistest.cron;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring_server.xml")
public class RedisPingTest {
	@Autowired
	RedisPing redisPing;
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		redisPing.closeRedisClient();
	}
	
	@Test(timeout = 60000)
	public void pingStatus(){
//		System.out.println(redisPing.getRedis().set("abc", "dd1"));
//		System.out.println(redisPing.getRedis().set("abc", "dd2"));
//		redisPing.closeRedisClient();
//		System.out.println(redisPing.getRedis().set("abc", "dd3"));
//		System.out.println(redisPing.getRedis().set("abc", "dd4"));
//		redisPing.closeRedisClient();
		while(true){
		}
	}
}
