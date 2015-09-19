package com.github.thorqin.toolkit.service;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by thor on 3/12/15.
 */
public class TaskService<T> {

    public static interface TaskHandler<T> {
        void process(T task);
    }

    private final ExecutorService executorService;
    private final TaskHandler<T> handler;
    private final AtomicInteger offerTaskCount = new AtomicInteger(0);

    public TaskService(TaskHandler<T> handler) {
        this(handler, 1);
    }

    public TaskService(TaskHandler<T> handler, int threadCount) {
        this.handler = handler;
        this.executorService = Executors.newFixedThreadPool(threadCount);
    }

    public void offer(final T task) {
        offerTaskCount.incrementAndGet();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                offerTaskCount.decrementAndGet();
                if (handler != null)
                    handler.process(task);
            }
        });
    }

    public int getOfferCount() {
        return offerTaskCount.get();
    }

    public void shutdown() throws InterruptedException {
        shutdown(Long.MAX_VALUE);
    }

    public void shutdown(long waitTime) throws InterruptedException {
        executorService.shutdown();
        executorService.awaitTermination(waitTime, TimeUnit.SECONDS);
    }

    public List<Runnable> shutdownNow() {
        return executorService.shutdownNow();
    }
}
