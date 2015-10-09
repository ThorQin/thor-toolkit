package com.github.thorqin.toolkit.redis;

import com.github.thorqin.toolkit.service.ISettingComparable;
import com.github.thorqin.toolkit.service.IStoppable;
import com.github.thorqin.toolkit.trace.Tracer;
import com.github.thorqin.toolkit.utility.ConfigManager;
import com.github.thorqin.toolkit.utility.Serializer;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.joda.time.DateTime;
import redis.clients.jedis.*;

import java.util.*;

public class RedisService implements IStoppable, ISettingComparable {

    public static class RedisSetting {
        /* Server URI:
         * redis://[user:password@]<address>[:<port>][/<db index, default: 0>]
         */
        public List<String> servers = new LinkedList<>();
        public int connectionTimeout = 2000;
        public int maxTotal = GenericObjectPoolConfig.DEFAULT_MAX_TOTAL;
        public int minIdle = GenericObjectPoolConfig.DEFAULT_MIN_IDLE;
        public int maxIdle = GenericObjectPoolConfig.DEFAULT_MAX_IDLE;
        public long maxWaitMillis = GenericObjectPoolConfig.DEFAULT_MAX_WAIT_MILLIS;
        public boolean testOnCreate = GenericObjectPoolConfig.DEFAULT_TEST_ON_CREATE;
        public boolean testOnReturn = GenericObjectPoolConfig.DEFAULT_TEST_ON_RETURN;
        public boolean testOnBorrow = GenericObjectPoolConfig.DEFAULT_TEST_ON_BORROW;
        public boolean testWhileIdle = GenericObjectPoolConfig.DEFAULT_TEST_WHILE_IDLE;
        public boolean blockWhenExhausted = GenericObjectPoolConfig.DEFAULT_BLOCK_WHEN_EXHAUSTED;
        public boolean fairness = GenericObjectPoolConfig.DEFAULT_FAIRNESS;
        public boolean lifo = GenericObjectPoolConfig.DEFAULT_LIFO;
    }

    // private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private RedisSetting setting;
    private final ShardedJedisPool pool;

    public class RedisSession implements AutoCloseable {
        private ShardedJedis jedis;

        public RedisSession(ShardedJedis jedis) {
            this.jedis = jedis;
        }

        @Override
        public void close() {
            jedis.close();
        }

        public String getString(final String key) {
            return jedis.get(key);
        }

        public String getString(final String key, final String defaultValue) {
            String value = jedis.get(key);
            if (value == null)
                return defaultValue;
            else
                return value;
        }

        public String getOrSetString(final String key, final String defaultValue) {
            String value = jedis.get(key);
            while (value == null) {
                if (jedis.setnx(key, defaultValue) == 1)
                    return defaultValue;
                else
                    value = jedis.get(key);
            }
            return value;
        }

        public <T> T get(final String key, final Class<T> type) {
            String value = jedis.get(key);
            if (value == null)
                return null;
            return Serializer.fromJson(value, type);
        }

        public <T> T get(final String key, final Class<T> type, final T defaultValue) {
            String value = jedis.get(key);
            if (value == null)
                return defaultValue;
            return Serializer.fromJson(value, type);
        }

        public <T> T getOrSet(final String key, final Class<T> type, final T defaultValue) {
            String value = jedis.get(key);
            while (value == null) {
                if (jedis.setnx(key, Serializer.toJsonString(defaultValue)) == 1)
                    return defaultValue;
                else
                    value = jedis.get(key);
            }
            return Serializer.fromJson(value, type);
        }

        public void setString(final String key, final String value) {
            jedis.set(key, value);
        }

        public void setString(final String key, final String value, final int expireSeconds) {
            jedis.setex(key, expireSeconds, value);
        }

        public <T> void set(final String key, final T value) {
            jedis.set(key, Serializer.toJsonString(value));
        }

        public <T> void set(final String key, T value, final int expireSeconds) {
            jedis.setex(key, expireSeconds, Serializer.toJsonString(value));
        }

        public void expire(final String key, final int expireSeconds) {
            jedis.expire(key, expireSeconds);
        }

        public void expireAt(final String key, final DateTime expireTime) {
            jedis.expireAt(key, expireTime.getMillis());
        }

        public void expireAt(final String key, final long expireTime) {
            jedis.expireAt(key, expireTime);
        }

        public boolean exists(final String key) {
            return jedis.exists(key);
        }

        public void delete(final String key) {
            jedis.del(key);
        }

        public long remainTime(final String key) {
            return jedis.ttl(key);
        }

        public long getLong(final String key) {
            String value = jedis.get(key);
            if (value == null || !value.matches("(\\+|-)?[0-9]+"))
                return 0L;
            else {
                return Long.parseLong(value);
            }
        }

        public long getLong(final String key, final long defaultValue) {
            String value = jedis.get(key);
            if (value == null || !value.matches("(\\+|-)?[0-9]+"))
                return defaultValue;
            else {
                return Long.parseLong(value);
            }
        }

        public long getOrSetLong(final String key, final long defaultValue) {
            String value = jedis.get(key);
            while (value == null || !value.matches("(\\+|-)?[0-9]+")) {
                if (jedis.setnx(key, String.valueOf(defaultValue)) == 1)
                    return defaultValue;
                else
                    value = jedis.get(key);
            }
            return Long.parseLong(value);
        }

        public void setLong(final String key, final long value) {
            jedis.set(key, String.valueOf(value));
        }

        public void setLong(final String key, final long value, final int expireSeconds) {
            jedis.setex(key, expireSeconds, String.valueOf(value));
        }

        public long increment(final String key) {
            return jedis.incr(key);
        }

        public long increment(final String key, final long step) {
            return jedis.incrBy(key, step);
        }

        public long decrement(final String key) {
            return jedis.decr(key);
        }

        public long decrement(final String key, final long step) {
            return jedis.decrBy(key, step);
        }
    }


    @Override
    public boolean isSettingChanged(ConfigManager configManager, String configName) {
        RedisService.RedisSetting newSetting = configManager.get(configName, RedisService.RedisSetting.class);
        return !Serializer.equals(newSetting, setting);
    }

    public RedisService(ConfigManager configManager, String configName, Tracer tracer) {
        this(configManager.get(configName, RedisService.RedisSetting.class), tracer);
    }

	public RedisService(RedisSetting setting) {
		this(setting, null);
	}

    public RedisService(RedisSetting setting, Tracer tracer) {
        this.setting = setting;

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMinIdle(setting.minIdle);
        poolConfig.setMaxTotal(setting.maxTotal);
        poolConfig.setMaxIdle(setting.maxIdle);
        poolConfig.setMaxWaitMillis(setting.maxWaitMillis);
        poolConfig.setTestOnBorrow(setting.testOnBorrow);
        poolConfig.setTestOnCreate(setting.testOnCreate);
        poolConfig.setTestWhileIdle(setting.testWhileIdle);
        poolConfig.setTestOnReturn(setting.testOnReturn);
        poolConfig.setBlockWhenExhausted(setting.blockWhenExhausted);
        poolConfig.setFairness(setting.fairness);
        poolConfig.setLifo(setting.lifo);

        List<JedisShardInfo> shards = new ArrayList<>(setting.servers.size());
        if (setting.servers != null) {
            for (String server : setting.servers) {
                JedisShardInfo info = new JedisShardInfo(server);
                info.setConnectionTimeout(setting.connectionTimeout);
                shards.add(info);
            }
        }
        if (shards.size() < 1){
            throw new ServiceConfigurationError("At least specify one redis server.");
        }
        pool = new ShardedJedisPool(poolConfig, shards);
    }

    public RedisSession getSession() {
        return new RedisSession(pool.getResource());
    }

    public int getNumActive() {
        return pool.getNumActive();
    }

    public int getNumIdle() {
        return pool.getNumIdle();
    }

    public int getNumWaiters() {
        return pool.getNumWaiters();
    }

    @Override
    public void stop() {
        if (!pool.isClosed())
            pool.close();
    }


}
