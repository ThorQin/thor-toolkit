package com.github.thorqin.toolkit;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by thor on 10/26/16.
 * Used to implements system daemon service via apache common-daemon utility
 */
public class ApplicationLoader implements Daemon, SignalHandler {

    private List<Application> appList = new LinkedList<>();

    private static ApplicationLoader loader = new ApplicationLoader();
    public static void main(String[] argv) throws Exception {
        loader.init(null);
        if (argv.length == 1 && argv[0].equals("start")) {
            loader.start();
        } else if (argv.length == 1 && argv[0].equals("stop")) {
            loader.stop();
        } else {
            Thread stopThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        synchronized (loader) {
                            loader.notify();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            stopThread.setDaemon(true);
            Runtime.getRuntime().addShutdownHook(stopThread);
            Signal.handle(new Signal("INT"), loader);
            Signal.handle(new Signal("TERM"), loader);
            loader.start();
            synchronized (loader) {
                System.out.println("Press 'Ctrl+C' to exit ...");
                loader.wait();
                System.out.println("\n");
            }
            loader.stop();
        }
    }

    @Override
    public void init(DaemonContext daemonContext) throws DaemonInitException, Exception {
        Application.createApplication(appList);
    }

    @Override
    public void start() throws Exception {
        for (Application app: appList) {
            app.onStartup();
        }
    }

    @Override
    public void stop() throws Exception {
        for (Application app: appList) {
            app.destroy();
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public void handle(Signal signal) {
        try {
            synchronized (loader) {
                loader.notify();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
