/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.thorqin.toolkit.log;

import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author nuo.qin
 */
public abstract class LogService implements Logger {
	private boolean alive = false;
	private Thread thread = null;
	private final LinkedBlockingQueue<Logger.LogInfo> logQueue = new LinkedBlockingQueue<>();
	private static final Logger.LogInfo stopSignal = new Logger.LogInfo();

	protected abstract void onLog(Logger.LogInfo info);

	public synchronized void start() {
		if (alive)
			return;
		alive = true;
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (alive || !logQueue.isEmpty()) {
					try {
						Logger.LogInfo info = logQueue.take();
						if (info != stopSignal)
							onLog(info);
						else
							alive = false;
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		});
		// After server shutdown keep the thread running until all task is finished.
		thread.setDaemon(false);
		thread.start();
	}
	public synchronized void stop() {
		if (!alive)
			return;
		alive = false;
		logQueue.offer(stopSignal);
	}

	@Override
	public final void log(Logger.LogInfo info) {
		logQueue.offer(info);
	}
}
