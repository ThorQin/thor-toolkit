package com.github.thorqin.toolkit.utility;

import com.github.thorqin.toolkit.service.TaskService;
import org.joda.time.DateTime;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * Created by thor on 11/6/15.
 */
public class LogHandler extends Handler {

    private Path logPath;
    private String name;
    private String fileDate = null;
    private boolean async;
    private boolean alive = true;

    private File realFile = null;
    private FileChannel channel = null;
    private FileLock lock = null;
    private TaskService<LogRecord> taskService = null;
    private TaskService.TaskHandler<LogRecord> taskHandler = new TaskService.TaskHandler<LogRecord>() {
        @Override
        public void process(LogRecord record) {
            writeLog(record);
        }
    };

    public LogHandler(String path, String name, boolean async) {
        this(new File(path).toPath(), name, async);
    }

    public LogHandler(Path path, String name, boolean async) {
        this.logPath = path;
        this.name = name;
        this.async = async;
        if (async) {
            taskService = new TaskService<>(taskHandler, 1);
        }
        try {
            this.setEncoding("utf-8");
        } catch (UnsupportedEncodingException e) {
        }
        this.setFormatter(new SimpleFormatter());
    }

    private void createChannel() throws IOException {
        if (lock != null) {
            lock.close();
            lock = null;
        }
        Files.createDirectories(logPath);
        int tryCount = 0;
        while (lock == null) {
            if (channel != null)
                channel.close();
            String suffix = "";
            if (tryCount > 0)
                suffix = "#" + String.valueOf(tryCount);
            Path realPath = logPath.resolve(name + " " + fileDate + suffix + ".log");
            realFile = realPath.toFile();
            channel = FileChannel.open(realPath, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            lock = channel.tryLock();
            tryCount++;
        }
    }

    private synchronized void writeLog(LogRecord record) {
        try {
            DateTime now = new DateTime(record.getMillis());
            String dateStr = now.toString("yyyy-MM-dd");
            if (!dateStr.equals(fileDate) || channel == null || !realFile.isFile()) {
                fileDate = dateStr;
                createChannel();
            }
            StringWriter writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            printWriter.append("[");
            printWriter.append(now.toString());
            printWriter.append("] ");
            if (record.getSourceClassName() != null) {
                printWriter.append(record.getSourceClassName());
                if (record.getSourceMethodName() != null) {
                    printWriter.append("::");
                    printWriter.append(record.getSourceMethodName());
                }
            }
            printWriter.println();
            printWriter.append(record.getLevel().toString());
            printWriter.append(": ");
            printWriter.append(record.getMessage());
            printWriter.println();
            if (record.getThrown() != null) {
                record.getThrown().printStackTrace(printWriter);
            }
            printWriter.println();
            channel.write(ByteBuffer.wrap(writer.toString().getBytes(this.getEncoding())));
            if (record.getLevel().intValue() >= Level.INFO.intValue()) {
                channel.force(false);
            }
            if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                System.err.println(writer.toString());
            } else if (record.getLevel().intValue() >= Level.CONFIG.intValue()) {
                System.out.println(writer.toString());
            }
        } catch (Exception e) {
            System.err.println("Write log failed: " + e.toString());
        }
    }

    @Override
    public void publish(LogRecord record) {
        if (!alive)
            return;
        if (async) {
            taskService.offer(record);
        } else {
            writeLog(record);
        }
    }

    @Override
    public void flush() {
        if (async) {
        } else {
            synchronized (this) {
                try {
                    channel.force(false);
                } catch (IOException e) {
                    System.err.println("Flush log to disk failed: " + e.toString());
                }
            }
        }
    }

    @Override
    public void close() throws SecurityException {
        alive = false;
        if (async) {
            try {
                taskService.shutdown(3000);
            } catch (InterruptedException e) {
                System.err.println("Shutdown log service error: " + e.toString());
            }
        }
        synchronized (this) {
            try {
                if (lock != null) {
                    lock.close();
                }
            } catch (IOException ex) {
                System.err.println("Close log file error: " + ex.toString());
            }
            try {
                if (channel != null)
                    channel.close();
            } catch (IOException ex) {
                System.err.println("Close log file error: " + ex.toString());
            }
        }
    }
}
