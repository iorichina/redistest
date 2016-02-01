package com.iorichina.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.exception.MemcachedException;
import net.rubyeye.xmemcached.transcoders.StringTranscoder;
import net.rubyeye.xmemcached.utils.AddrUtil;

public class MemcachedOperate {
	final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(MemcachedOperate.class);

	// single
	private String memcacheservers;
	private List<MemcachedClient> memcachedClients = new ArrayList<MemcachedClient>();

	// batch
	private Map<String, String> memcacheserverMaps;
	private Map<String, List<MemcachedClient>> memcachedClientMaps = new HashMap<String, List<MemcachedClient>>();

	/**
	 * single init
	 */
	public void initSingle() {

		if (getMemcacheservers() == null || getMemcacheservers().isEmpty()) {
			log.error("memcacheservers config is empty");
			return;
		}
		String[] serverStrings = getMemcacheservers().split(",");
		for (String server : serverStrings) {
			try {
				MemcachedClientBuilder builder = new XMemcachedClientBuilder(
						AddrUtil.getAddresses(server));
				builder.setTranscoder(new StringTranscoder("UTF-8"));
				memcachedClients.add(builder.build());
			} catch (Exception e) {
				log.error("builder MemcachedClientBuilder error,server="
						+ server + ",e＝", e);
			}

		}
	}

	/**
	 * bach init
	 */
	public void init() {
		for (String key : getMemcacheserverMaps().keySet()) {
			if (memcacheserverMaps.get(key) == null
					|| memcacheserverMaps.get(key).isEmpty()) {
				log.error("memcacheservers config is empty , key =>" + key);
				continue;
			}
			String[] serverStrings = memcacheserverMaps.get(key).split(",");
			List<MemcachedClient> cliList = new ArrayList<MemcachedClient>();
			for (String server : serverStrings) {
				try {
					MemcachedClientBuilder builder = new XMemcachedClientBuilder(
							AddrUtil.getAddresses(server));
					builder.setTranscoder(new StringTranscoder("UTF-8"));
					builder.setConnectionPoolSize(5);
					cliList.add(builder.build());
				} catch (Exception e) {
					log.error("builder MemcachedClientBuilder error,server="
							+ server + ",e＝", e);
				}
			}
			memcachedClientMaps.put(key, cliList);
		}
	}

	/**
	 * close all group in single and batch
	 */
	public void close() {
		// single
		for (MemcachedClient client : memcachedClients) {
			try {
				// close memcached client
				client.shutdown();
			} catch (IOException e) {
				log.error("shutdown MemcachedClient error ,e=>", e);
			}
		}
		memcachedClients.clear();

		// batch
		for (String key : memcachedClientMaps.keySet()) {
			try {
				// close memcached client
				for (MemcachedClient cli : memcachedClientMaps.get(key)) {
					cli.shutdown();
				}
			} catch (IOException e) {
				log.error("shutdown MemcachedClient error ,e=>", e);
			}
		}
		memcachedClientMaps.clear();
	}

	/**
	 * set to single group
	 * 
	 * @param key
	 * @param timeout
	 * @param value
	 * @throws Exception
	 */
	public void memcacheSet(String key, int timeout, String value)
			throws Exception {
		for (MemcachedClient client : memcachedClients) {
			try {
				client.set(key, timeout, value);
			} catch (MemcachedException e) {
				log.error("MemcachedClient operation fail, set key=" + key);
			}
		}
	}

	/**
	 * get from single group
	 * 
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public Object memcacheGet(String key) throws Exception {
		for (MemcachedClient client : memcachedClients) {
			Object value = client.get(key);
			if (value != null)
				return value;
		}
		return null;
	}

	/**
	 * set to all group in batch
	 * 
	 * @param key
	 * @param value
	 * @param timeout
	 * @throws Exception
	 */
	public void pushAll(String key, String value, int timeout) throws Exception {
		for (String cliKey : memcachedClientMaps.keySet()) {
			push(cliKey, key, value, timeout);
		}
	}

	/**
	 * set to some group in batch
	 * 
	 * @param cliKeys
	 * @param key
	 * @param value
	 * @param timeout
	 * @throws Exception
	 */
	public void pushPart(List<String> cliKeys, String key, String value,
			int timeout) throws Exception {
		for (String client : cliKeys) {
			push(client, key, value, timeout);
		}
	}

	/**
	 * set to group in batch
	 * 
	 * @param opt
	 * @param key
	 * @param value
	 * @param timeout
	 * @throws Exception
	 */
	public void push(String opt, String key, String value, int timeout)
			throws Exception {
		for (MemcachedClient client : memcachedClientMaps.get(opt)) {
			try {
				client.set(key, timeout, value);
			} catch (MemcachedException e) {
				log.error("MemcachedClient operation fail, set key=" + key
						+ ", e=>", e);
			}
		}
	}

	/**
	 * get from all group of batch
	 * 
	 * @param key
	 * @return
	 * @throws TimeoutException
	 * @throws InterruptedException
	 * @throws MemcachedException
	 */
	public Object pullAll(String key) throws TimeoutException,
			InterruptedException, MemcachedException {
		Object value = null;
		for (String client : memcachedClientMaps.keySet()) {
			value = pull(client, key);
		}
		return value;
	}

	/**
	 * get from some group of batch
	 * 
	 * @param cliKeys
	 * @param key
	 * @return
	 * @throws TimeoutException
	 * @throws InterruptedException
	 * @throws MemcachedException
	 */
	public Object pullPart(List<String> cliKeys, String key)
			throws TimeoutException, InterruptedException, MemcachedException {
		Object value = null;
		for (String client : cliKeys) {
			value = pull(client, key);
		}
		return value;
	}

	/**
	 * get from group in batch
	 * 
	 * @param opt
	 * @param key
	 * @return
	 * @throws TimeoutException
	 * @throws InterruptedException
	 * @throws MemcachedException
	 */
	public Object pull(String opt, String key) throws TimeoutException,
			InterruptedException, MemcachedException {
		for (MemcachedClient client : memcachedClientMaps.get(opt)) {
			Object value = client.get(key);
			if (value != null)
				return value;
		}
		return null;
	}

	public void setMemcacheservers(String memcacheservers) {
		this.memcacheservers = memcacheservers;
	}

	public String getMemcacheservers() {
		return memcacheservers;
	}

	public List<MemcachedClient> getMemcachedClients() {
		return memcachedClients;
	}

	public Map<String, String> getMemcacheserverMaps() {
		return memcacheserverMaps;
	}

	public void setMemcacheserverMaps(Map<String, String> memcacheserverMaps) {
		this.memcacheserverMaps = memcacheserverMaps;
	}

	public Map<String, List<MemcachedClient>> getMemcachedClientMaps() {
		return memcachedClientMaps;
	}

	public void setMemcachedClientMaps(
			Map<String, List<MemcachedClient>> memcachedClientMaps) {
		this.memcachedClientMaps = memcachedClientMaps;
	}
}
