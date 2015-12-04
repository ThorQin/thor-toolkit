package com.github.thorqin.toolkit.redis;

import com.github.thorqin.toolkit.annotation.Service;
import com.github.thorqin.toolkit.service.IService;
import com.github.thorqin.toolkit.utility.ConfigManager;
import com.github.thorqin.toolkit.utility.Localization;
import com.github.thorqin.toolkit.utility.Serializer;
import com.github.thorqin.toolkit.validation.ValidateException;
import com.github.thorqin.toolkit.validation.Validator;
import com.github.thorqin.toolkit.validation.annotation.ValidateCollection;
import com.github.thorqin.toolkit.validation.annotation.ValidateString;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.joda.time.DateTime;
import redis.clients.jedis.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RedisService implements IService {

    public static class RedisSetting {
        /* Server URI:
         * redis://[user:password@]<address>[:<port>][/<db index, default: 0>]
         */
        @ValidateString("^redis://([a-zA-Z0-9_\\-\\.]+:[^@]+@)?[a-zA-Z0-9\\.\\-_]+(:[0-9]+)?(/[0-9]+)?$")
        @ValidateCollection(minSize = 1, type = String.class)
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

    private RedisSetting setting = null;
    private ShardedJedisPool pool = null;
    @Service("logger")
    private Logger logger = Logger.getLogger(RedisService.class.getName());
    private String serviceName = null;

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
    public boolean config(ConfigManager configManager, String serviceName, boolean isReConfig) {
        this.serviceName = serviceName;
        RedisSetting newSetting = configManager.get(serviceName, RedisSetting.class);
        try {
            validateSetting(newSetting);
        } catch (ValidateException ex) {
            logger.log(Level.SEVERE, "Invalid RedisService configuration settings: {0}", ex.getMessage());
            return false;
        }
        boolean needRestart = !Serializer.equals(newSetting, setting);
        setting = newSetting;
        return needRestart;
    }

    public RedisService() {
        setting = null;
    }

    public RedisService(RedisSetting setting) throws ValidateException {
        this.setting = setting;
        validateSetting(setting);
    }

    private void validateSetting(RedisSetting setting) throws ValidateException {
        Validator validator = new Validator(Localization.getInstance());
        validator.validateObject(setting, RedisSetting.class, false);
    }

    @Override
    public boolean isStarted() {
        return pool != null;
    }

    @Override
    public synchronized void start() {
        if (pool == null) {
            logger.log(Level.WARNING, "RedisService has already started! (Service name: {0})", serviceName);
            return;
        }
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
        pool = new ShardedJedisPool(poolConfig, shards);
    }

    @Override
    public synchronized void stop() {
        if (pool == null) {
            logger.log(Level.WARNING, "RedisService has already stopped! (Service name: {0})", serviceName);
            return;
        }
        if (!pool.isClosed())
            pool.close();
        pool = null;
    }

    public RedisSession getSession() {
        if (pool == null)
            throw new RuntimeException(
                    MessageFormat.format("RedisService is not started! (Service name: {0})", serviceName));
        return new RedisSession(pool.getResource());
    }

    public int getNumActive() {
        if (pool == null)
            throw new RuntimeException(
                    MessageFormat.format("RedisService is not started! (Service name: {0})", serviceName));
        return pool.getNumActive();
    }

    public int getNumIdle() {
        if (pool == null)
            throw new RuntimeException(
                    MessageFormat.format("RedisService is not started! (Service name: {0})", serviceName));
        return pool.getNumIdle();
    }

    public int getNumWaiters() {
        if (pool == null)
            throw new RuntimeException(
                    MessageFormat.format("RedisService is not started! (Service name: {0})", serviceName));
        return pool.getNumWaiters();
    }


}
