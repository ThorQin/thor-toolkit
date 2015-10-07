package com.github.thorqin.toolkit.redis;

import com.github.thorqin.toolkit.service.ISettingComparable;
import com.github.thorqin.toolkit.service.IStoppable;
import com.github.thorqin.toolkit.trace.Tracer;
import com.github.thorqin.toolkit.utility.ConfigManager;
import com.github.thorqin.toolkit.utility.Serializer;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

public class RedisService implements IStoppable, ISettingComparable {

    public static class RedisSetting {
        public String uri;
        public String user;
        public String password;
        public String address = "default";
        public boolean broadcast = false;
        public boolean trace = false;
    }

    private static final Logger logger =
            Logger.getLogger(RedisService.class.getName());

	private RedisSetting setting;
    private boolean isRunning = false;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();


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
//        if (Strings.isNullOrEmpty(setting.uri))
//            throw new ConfigurationException("Must provide the ActiveMQ URI info.");
//        if (!Strings.isNullOrEmpty(setting.user))
//            connectionFactory = new ActiveMQConnectionFactory(setting.user, setting.password, setting.uri);
//        else
//            connectionFactory = new ActiveMQConnectionFactory(setting.uri);
//        connectionFactory.setTransportListener(this);
//        connection = null;
    }

    @Override
    public void stop() {
        if (!isRunning)
            return;
        isRunning = false;
    }


}
