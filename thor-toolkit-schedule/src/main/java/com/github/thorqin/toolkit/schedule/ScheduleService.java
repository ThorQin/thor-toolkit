package com.github.thorqin.toolkit.schedule;

import com.github.thorqin.toolkit.Application;
import com.github.thorqin.toolkit.annotation.Service;
import com.github.thorqin.toolkit.schedule.annotation.ScheduleJob;
import com.github.thorqin.toolkit.service.ISettingComparable;
import com.github.thorqin.toolkit.service.IStartable;
import com.github.thorqin.toolkit.service.IStoppable;
import com.github.thorqin.toolkit.trace.Tracer;
import com.github.thorqin.toolkit.utility.ConfigManager;
import com.github.thorqin.toolkit.utility.Serializer;
import com.google.common.base.Strings;
import org.quartz.*;
import org.quartz.impl.DirectSchedulerFactory;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by thor on 10/16/15.
 */
public class ScheduleService implements IStartable, IStoppable, ISettingComparable {

    public static class JobInfo {
        public String action;
        public List<String> schedule;
        public boolean enable = true;
    }

    public static class Setting {
        public int workers = 1; // How many working thread.
        public Map<String, JobInfo> jobs = null;
        public boolean trace = false;
    }

    private Setting setting;
    private Tracer tracer = null;
    private Scheduler scheduler;

    @Service("logger")
    private Logger logger = Logger.getLogger(ScheduleService.class.getName());

    @Service("application")
    private Application application = null;

    public ScheduleService(ConfigManager configManager, String configName, Tracer tracer) throws SchedulerException {
        this(configManager.get(configName, Setting.class), tracer);
    }

    public ScheduleService(Setting setting) throws SchedulerException {
        this(setting, null);
    }

    public ScheduleService(Setting setting, Tracer tracer) throws SchedulerException {
        this.setting = (setting == null ? new Setting() : setting);
        this.tracer = tracer;
        DirectSchedulerFactory factory = DirectSchedulerFactory.getInstance();
        factory.createVolatileScheduler(this.setting.workers);
        this.scheduler = factory.getScheduler();
    }

    public synchronized void setTracer(Tracer tracer) {
        this.tracer = tracer;
    }

    private void init() {
        // Scan service registry first
        if (application == null)
            return;

        Set<String> keys = application.getServiceKeys();
        for (String key: keys) {
            Object service = application.getService(key);
            if (service == this)
                continue;
            Method[] methods = service.getClass().getDeclaredMethods();
            for (Method method: methods) {
                ScheduleJob scheduleJob = method.getAnnotation(ScheduleJob.class);
                if (scheduleJob == null)
                    continue;
                try {
                    addJob(scheduleJob.name(), key + "::" + method.getName(), scheduleJob.schedule());
                }catch (SchedulerException e) {
                    logger.log(Level.WARNING, "Add job by service pre-definition.", e);
                }
            }
        }

        // Then use config setting replace the conflict item(same job name)
        if (setting.jobs == null)
            return;
        for (String jobName: setting.jobs.keySet()) {
            JobInfo jobInfo = setting.jobs.get(jobName);
            if (jobInfo.enable) {
                if (Strings.isNullOrEmpty(jobName) ||
                        Strings.isNullOrEmpty(jobInfo.action) ||
                        jobInfo.schedule == null) {
                    logger.log(Level.WARNING, "Invalid job definition in config setting.");
                    continue;
                }
                try {
                    addJob(jobName, jobInfo.action, jobInfo.schedule.toArray(new String[jobInfo.schedule.size()]));
                } catch (SchedulerException e) {
                    logger.log(Level.WARNING, "Add job by config setting failed.", e);
                }
            } else {
                try {
                    removeJob(jobName);
                } catch (SchedulerException e) {
                    logger.log(Level.WARNING, "Remove job by config setting failed.", e);
                }
            }
        }
    }

    @Override
    public boolean isSettingChanged(ConfigManager configManager, String configName) {
        Setting newSetting = configManager.get(configName, Setting.class);
        if (newSetting == null)
            newSetting = new Setting();
        return !Serializer.equals(newSetting, setting);
    }

    @Override
    public void start() {
        try {
            if (scheduler.isStarted())
                return;
            init();
            scheduler.start();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        if (setting.trace && tracer != null) {
            Tracer.Info info = new Tracer.Info();
            info.catalog = "schedule service";
            info.name = "started";
            tracer.trace(info);
        }
    }

    public static class JobWrapper implements Job {
        private Logger logger = Logger.getLogger(ScheduleService.class.getName());
        public JobWrapper() {
        }
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            boolean result = false;
            boolean trace = false;
            Application application = null;
            try {
                JobDataMap dataMap = context.getJobDetail().getJobDataMap();
                String jobDefine = dataMap.getString("jobDefine");
                String appName = dataMap.getString("appName");
                trace = dataMap.getBoolean("trace");
                application = Application.get(appName);
                if (application == null) {
                    logger.log(Level.WARNING, "Should run under the application environment.");
                    return;
                }
                logger = application.getLogger();
                String[] arr = jobDefine.split("::");
                Object serviceInstance = application.getService(arr[0]);
                if (serviceInstance == null) {
                    logger.log(Level.WARNING, "Job target not found: no specified service found.");
                    return;
                }
                Class<?> clazz = serviceInstance.getClass();
                Method method = clazz.getDeclaredMethod(arr[1]);
                method.setAccessible(true);
                method.invoke(serviceInstance);
                result = true;
            } catch (NoSuchMethodException e) {
                logger.log(Level.WARNING, "Job target not found: no specified method found.");
            } catch (Exception e) {
                logger.log(Level.WARNING, "Job execute failed.", e);
            } finally {
                if (trace && application != null) {
                    Tracer.Info info = new Tracer.Info();
                    info.put("success", result);
                    info.catalog = "schedule service";
                    info.name = "Job: " + context.getJobDetail().getKey().getName();
                    application.getTracer().trace(info);
                }
            }
        }
    }

    public void addJob(String jobName, String jobDefine, String...crons) throws SchedulerException {
        if (application == null)
            throw new SchedulerException("Should run under the application environment.");
        String[] arr = jobDefine.split("::");
        if (arr.length != 2) {
            throw new SchedulerException("Invalid execute target, should be 'SERVICE_NAME::METHOD' form");
        }

        if (scheduler.checkExists(JobKey.jobKey(jobName))) {
            removeJob(jobName);
        }

        JobDetail jobDetail = JobBuilder
                .newJob(JobWrapper.class)
                .withIdentity(jobName)
                .usingJobData("jobDefine", jobDefine)
                .usingJobData("appName", application.getName())
                .usingJobData("trace", setting.trace)
                .build();

        Set<Trigger> triggers = new HashSet<>();
        int i = 0;
        for (String cron: crons) {
            try {
                Trigger trigger = TriggerBuilder.newTrigger()
                        .forJob(jobDetail)
                        .withIdentity(jobName + "_" + String.valueOf(i++), jobName)
                        .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                        .build();
                triggers.add(trigger);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Add job trigger failed: '" + jobName + "': " + ex.getMessage());
            }
        }
        if (triggers.size() > 0)
            scheduler.scheduleJob(jobDetail, triggers, true);
        else
            logger.log(Level.WARNING, "Add job failed: '" + jobName + "' does not has any valid triggers.");
    }

    public void removeJob(String jobName) throws SchedulerException {
        List<? extends Trigger> triggers = scheduler.getTriggersOfJob(JobKey.jobKey(jobName));
        List<TriggerKey> triggerKeys = new ArrayList<>(triggers.size());
        for (Trigger trigger: triggers) {
            triggerKeys.add(trigger.getKey());
        }
        scheduler.unscheduleJobs(triggerKeys);
        scheduler.deleteJob(JobKey.jobKey(jobName));
    }

    public void removeAllJobs() throws SchedulerException {
        scheduler.clear();
    }

    @Override
    public void stop() {
        try {
            if (!scheduler.isStarted())
                return;
            scheduler.clear();
            scheduler.shutdown();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        if (setting.trace && tracer != null) {
            Tracer.Info info = new Tracer.Info();
            info.catalog = "schedule service";
            info.name = "stopped";
            tracer.trace(info);
        }
    }
}
