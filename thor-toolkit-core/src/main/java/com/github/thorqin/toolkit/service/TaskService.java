package com.github.thorqin.toolkit.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by thor on 3/12/15.
 */
public class TaskService<T> {

    public static interface TaskHandler<T> {
        void process(T task);
    }

    private final ExecutorService executorService;
    private final TaskHandler<T> handler;

    public TaskService(TaskHandler<T> handler) {
        this(handler, 1);
    }

    public TaskService(TaskHandler<T> handler, int threadCount) {
        this.handler = handler;
        this.executorService = Executors.newFixedThreadPool(threadCount);
    }

    public void offser(final T task) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (handler != null)
                    handler.process(task);
            }
        });
    }

    public void shutdown() throws InterruptedException {
        shutdown(Long.MAX_VALUE);
    }

    public void shutdown(long waitTime) throws InterruptedException {
        executorService.shutdown();
        executorService.awaitTermination(waitTime, TimeUnit.SECONDS);
    }
}
