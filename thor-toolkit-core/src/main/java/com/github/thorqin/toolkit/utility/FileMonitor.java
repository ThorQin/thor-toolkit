package com.github.thorqin.toolkit.utility;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by thor on 2015/10/1.
 */
public abstract class FileMonitor {
    private final static WatchService watchService;
    private final static Map<WatchKey, MonitorInfo> keyMap = new HashMap<>();
    private final static Map<Path, MonitorInfo> watchMap = new HashMap<>();
    private final static Runnable monitorRunnable = new Runnable() {
        private void lookup() throws InterruptedException, IOException {
            WatchKey key = watchService.take();
            synchronized (FileMonitor.class) {
                MonitorInfo monitorInfo = keyMap.get(key);
                if (monitorInfo == null) {
                    key.cancel();
                    return;
                }
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }
                    Path eventPath = (Path)event.context();
                    //Path eventPath = ((WatchEvent<Path>) event).context();
                    ChangeType changeType;
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        changeType = ChangeType.CREATE;
                    } else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                        changeType = ChangeType.DELETE;
                    } else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                        changeType = ChangeType.MODIFY;
                    } else {
                        changeType = null;
                    }
                    monitorInfo.change(eventPath, changeType);
                }
                if (!key.reset()) {
                    monitorInfo.unwatch();
                    monitorInfo.watch();
                }
            }
        }

        @Override
        public void run() {
            for (; ; ) {
                try {
                    lookup();
                } catch (InterruptedException e) {
                    break;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    break;
                }
            }
        }
    };

    public static class Monitor implements AutoCloseable {
        private final FileChangeListener changeListener;
        private final Object param;
        private final File file;
        final Path path;
        boolean exists;
        private Monitor(File file, FileChangeListener changeListener, Object param) {
            this.changeListener = changeListener;
            this.param = param;
            this.file = file;
            this.path = file.toPath().getFileName();
            updateState();
        }

        void updateState() {
            this.exists = file.exists();
        }

        private void raise(ChangeType changeType) {
            try {
                if (changeListener != null)
                    changeListener.onFileChange(file, changeType, param);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        @Override
        public void close() {
            synchronized (FileMonitor.class) {
                Path foldPath = file.toPath().getParent();
                MonitorInfo monitorInfo = watchMap.get(foldPath);
                if (monitorInfo != null) {
                    monitorInfo.removeFile(this);
                }
            }
        }
    }

    private static class MonitorInfo {
        public Path path = null;
        public Path parentPath = null;
        public WatchKey watchKey = null;
        public Map<Path, MonitorInfo> folderMonitors = null;
        public List<Monitor> fileMonitors = null;

        public void watch() throws IOException {
            if (path.toFile().isDirectory()) {
                watchKey = path.register(watchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_DELETE);
            }
            if (watchKey != null)
                keyMap.put(watchKey, this);
        }

        public void watchAll() throws IOException {
            watch();
            if (folderMonitors != null) {
                for (MonitorInfo folderMonitor: folderMonitors.values()) {
                    folderMonitor.watchAll();
                }
            }
            if (fileMonitors != null) {
                for (Monitor monitor : fileMonitors) {
                    monitor.updateState();
                    if (monitor.exists)
                        monitor.raise(ChangeType.CREATE);
                }
            }
        }

        public void unwatch() {
            if (watchKey != null) {
                keyMap.remove(watchKey);
                watchKey.cancel();
                watchKey = null;
            }
        }

        public void unwatchAll() throws IOException {
            unwatch();
            if (folderMonitors != null) {
                for (MonitorInfo folderMonitor: folderMonitors.values()) {
                    folderMonitor.unwatchAll();
                }
            }
            if (fileMonitors != null) {
                for (Monitor monitor : fileMonitors) {
                    if (monitor.exists)
                        monitor.raise(ChangeType.DELETE);
                    monitor.updateState();
                }
            }
        }

        public void change(Path changePath, ChangeType changeType) throws IOException {
            if (folderMonitors != null) {
                MonitorInfo monitorInfo = folderMonitors.get(changePath);
                if (monitorInfo != null) {
                    switch (changeType) {
                        case CREATE:
                            monitorInfo.watchAll();
                            break;
                        case MODIFY:
                            break;
                        case DELETE:
                            monitorInfo.unwatchAll();
                            break;
                    }
                    return;
                }
            }
            if (fileMonitors != null) {
                for (Monitor monitor : fileMonitors) {
                    if (monitor.path.compareTo(changePath) == 0) {
                        monitor.updateState();
                        monitor.raise(changeType);
                    }
                }
            }
        }

        public void addFile(Monitor monitor) {
            if (fileMonitors == null)
                fileMonitors = new LinkedList<>();
            fileMonitors.add(monitor);
        }

        public void removeFile(Monitor monitor) {
            if (fileMonitors != null) {
                fileMonitors.remove(monitor);
            }
            cleanup();
        }

        public void removeFolder(Path path) {
            Path key = path.getFileName();
            if (folderMonitors != null) {
                folderMonitors.remove(key);
            }
            cleanup();
        }

        public void addFolder(Path path, MonitorInfo monitorInfo) {
            if (folderMonitors == null)
                folderMonitors = new HashMap<>();
            Path key = path.getFileName();
            if (!folderMonitors.containsKey(key))
                folderMonitors.put(key, monitorInfo);
        }

        public void cleanup() {
            if (fileMonitors != null && fileMonitors.isEmpty())
                fileMonitors = null;
            if (folderMonitors != null && folderMonitors.isEmpty())
                folderMonitors = null;
            if (fileMonitors == null && folderMonitors == null) {
                unwatch();
                watchMap.remove(path);
                if (parentPath != null) {
                    MonitorInfo parentInfo = watchMap.get(parentPath);
                    if (parentInfo != null) {
                        parentInfo.removeFolder(path);
                    }
                }
            }
        }
    }

    public enum ChangeType {
        CREATE,
        MODIFY,
        DELETE
    }

    public interface FileChangeListener {
        void onFileChange(File file, ChangeType changeType, Object param);
    }

    static {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Thread thread = new Thread(monitorRunnable);
            thread.setDaemon(true);
            thread.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private FileMonitor(){}

    private static void watchParent(Path foldPath, Path childPath, MonitorInfo childInfo) throws IOException {
        if (foldPath == null)
            return;
        MonitorInfo monitorInfo = watchMap.get(foldPath);
        if (monitorInfo == null) {
            monitorInfo = new MonitorInfo();
            monitorInfo.path = foldPath;
            monitorInfo.parentPath = foldPath.getParent();
            monitorInfo.addFolder(childPath, childInfo);
            monitorInfo.watch();
            watchMap.put(foldPath, monitorInfo);
            watchParent(foldPath.getParent(), foldPath, monitorInfo);
        } else {
            monitorInfo.addFolder(childPath, childInfo);
        }
    }

    public static Monitor watch(File file, FileChangeListener listener, Object param) throws IOException {
        Path foldPath = file.getAbsoluteFile().toPath().getParent();
        synchronized (FileMonitor.class) {
            MonitorInfo monitorInfo = watchMap.get(foldPath);
            Monitor monitor = new Monitor(file.getAbsoluteFile(), listener, param);
            if (monitorInfo == null) {
                monitorInfo = new MonitorInfo();
                monitorInfo.path = foldPath;
                monitorInfo.parentPath = foldPath.getParent();
                monitorInfo.addFile(monitor);
                monitorInfo.watch();
                watchMap.put(foldPath, monitorInfo);
                watchParent(foldPath.getParent(), foldPath, monitorInfo);
            } else {
                monitorInfo.addFile(monitor);
            }
            return monitor;
        }
    }

    public static Monitor watch(File file, FileChangeListener listener) throws IOException {
        return watch(file, listener, null);
    }

    public static Monitor watch(String file, FileChangeListener listener, Object param) throws IOException {
        return watch(new File(file), listener, param);
    }

    public static Monitor watch(String file, FileChangeListener listener) throws IOException {
        return watch(new File(file), listener, null);
    }
}
