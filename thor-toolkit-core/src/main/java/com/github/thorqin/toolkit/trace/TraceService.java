/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.thorqin.toolkit.trace;

import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author nuo.qin
 */
public abstract class TraceService implements Tracer {
	private boolean alive = false;
	private Thread thread = null;
	private final LinkedBlockingQueue<Info> queue = new LinkedBlockingQueue<>();
	private static final Info stopSignal = new Info();

	protected abstract void onTraceInfo(Info info);

	public final synchronized void start() {
		if (alive)
			return;
		alive = true;
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (alive || !queue.isEmpty()) {
					try {
						Info info = queue.take();
						if (info != stopSignal)
							onTraceInfo(info);
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
	public final synchronized void stop() {
		if (!alive)
			return;
		alive = false;
		queue.offer(stopSignal);
	}

	@Override
	public final void trace(Info info) {
		queue.offer(info);
	}
}
