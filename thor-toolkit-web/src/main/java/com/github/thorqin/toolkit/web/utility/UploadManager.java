/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.thorqin.toolkit.web.utility;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.github.thorqin.toolkit.utility.MimeUtils;
import com.github.thorqin.toolkit.utility.Serializer;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 *
 * @author nuo.qin
 */
public final class UploadManager {
	public final static int DEFAULT_MAX_SIZE = 1024 * 1024 * 10;
	private final String uploadDir;
//	private final Map<String, String> mime = new HashMap<>();
//    private boolean restrictMime = true;
    private Pattern pattern;
    private boolean storeDetail;
	private int maxUploadSize;

	public UploadManager(File baseDir) {
        this(baseDir, true, DEFAULT_MAX_SIZE);
	}

	public UploadManager(File baseDir, int maxUploadSize) {
		this(baseDir, true, maxUploadSize);
	}

    public UploadManager(File baseDir, boolean storeDetail, int maxUploadSize) {
        this.uploadDir = baseDir.getAbsolutePath();
        this.storeDetail = storeDetail;
		this.maxUploadSize = maxUploadSize;
        // By default, only allow upload images
		setFileSuffix("jpg|jpeg|png|gif");
    }

    /**
     * Set upload restriction
     * @param rule Regex expression that used to match the file name.
     */
    public void setRestriction(String rule) {
        if (rule == null)
            pattern = null;
        else
            pattern = Pattern.compile(rule);
    }

    public void setFileSuffix(String suffix) {
        if (suffix == null)
            pattern = null;
        else
            setRestriction("(?i).+\\.(" + suffix + ")");
    }

    public boolean isStoreDetail() {
        return storeDetail;
    }

    public void setStoreDetail(boolean storeDetail) {
        this.storeDetail = storeDetail;
    }

    public static class FileBasicInfo {
        public String fileId;
        public long createTime;
    }

	public static class FileInfo extends FileBasicInfo {
		public String fileName;
		public String mimeType;

		public void setFileName(String name) {
			fileName = name;
			if (fileName.lastIndexOf("\\") != -1) {
				fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
			}
			if (fileName.lastIndexOf("/") != -1) {
				fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
			}
		}
		public String getName() {
			if (fileName.contains(".")) {
				return fileName.substring(0, fileName.lastIndexOf("."));
			} else {
				return fileName;
			}
		}
		public String getExtName() {
			if (fileName.contains(".")) {
				return fileName.substring(fileName.lastIndexOf(".") + 1);
			} else {
				return "";
			}
		}
	}

    private String fileIdToPath(String fileId) {
        StringBuilder sb = new StringBuilder(40);
        int len = fileId.length(), i = 0, scan = 0;
        while (i < 7 && scan + 4 < len) {
            sb.append(fileId.substring(scan, scan + 4));
            sb.append('/');
            i++;
            scan += 4;
        }
        sb.append(fileId.substring(scan));
        return uploadDir + "/" + sb.toString();
    }

    private String filePathToId(File file) {
        String path = file.getAbsoluteFile().getPath();
        String basePath = new File(uploadDir).getAbsoluteFile().getPath();
        if (!path.startsWith(basePath)) {
            return null;
        }
        path = path.substring(basePath.length());
        path = path.replaceAll("/|\\\\","");
        if (path.endsWith(".data")) {
            path = path.substring(0, path.length() - 5);
        }
        return path;
    }

	public void deleteFile(String fileId) {
        String filePath = fileIdToPath(fileId);
        File jsonFile = new File(filePath + ".json");
        File folder = jsonFile.getParentFile();
        if (jsonFile.exists()) {
            jsonFile.delete();
        }
        File dataFile = new File(filePath + ".data");
        if (dataFile.exists()) {
            dataFile.delete();
        }
	}
	public void moveToDir(String fileId, String destDir) throws IOException {
		moveToDir(fileId, destDir, true);
	}
	public void moveToDir(String fileId, String destDir, boolean replaceExisting) throws IOException {
		String name = getFileInfo(fileId).fileName;
		moveToDir(fileId, destDir, name, replaceExisting);
	}
	public void moveToDir(String fileId, String destDir, String newName) throws IOException {
		moveToDir(fileId, destDir, newName, true);
	}
	public void moveToDir(String fileId, String destDir, String newName, boolean replaceExisting) throws IOException {
		File fileDir = new File(destDir);
		Files.createDirectories(fileDir.toPath());
		File path = new File(fileDir.toString() + "/" + newName);
		moveTo(fileId, path, replaceExisting);
	}
	public void moveTo(String fileId, File destPath) throws IOException {
		moveTo(fileId, destPath, true);
	}
	public void moveTo(String fileId, File destPath, boolean replaceExisting) throws IOException {
        copyTo(fileId, destPath, replaceExisting);
		deleteFile(fileId);
	}
	
	public void copyToDir(String fileId, String destDir) throws IOException {
		copyToDir(fileId, destDir, true);
	}
	public void copyToDir(String fileId, String destDir, boolean replaceExisting) throws IOException {
		String name = getFileInfo(fileId).fileName;
		copyToDir(fileId, destDir, name, replaceExisting);
	}
	public void copyToDir(String fileId, String destDir, String newName) throws IOException {
		copyToDir(fileId, destDir, newName, true);
	}
	public void copyToDir(String fileId, String destDir, String newName, boolean replaceExisting) throws IOException {
		File fileDir = new File(destDir);
		Files.createDirectories(fileDir.toPath());
		File path = new File(fileDir.toString() + "/" + newName);
		copyTo(fileId, path, replaceExisting);
	}
	public void copyTo(String fileId, File destPath) throws IOException {
		copyTo(fileId, destPath, true);
	}
	public void copyTo(String fileId, File destPath, boolean replaceExisting) throws IOException {
        String filePath = fileIdToPath(fileId);
        File dataFile = new File(filePath + ".data");
        File parentFile = destPath.getParentFile();
        if (parentFile != null)
            Files.createDirectories(parentFile.toPath());
		if (replaceExisting)
			Files.copy(dataFile.toPath(), destPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
		else
			Files.copy(dataFile.toPath(), destPath.toPath());
	}

	public FileInfo store(File originFile) throws IOException {
		return store(originFile, null);
	}

	public FileInfo store(File originFile, String mimeType) throws IOException {
		try (InputStream in = new FileInputStream(originFile)) {
			return store(in, originFile.getName(), mimeType);
		}
	}

	public FileInfo store(byte[] data, String fileName) throws IOException {
		return store(data, fileName, null);
	}

	public FileInfo store(byte[] data, String fileName, String mimeType) throws IOException {
		try (InputStream in = new ByteArrayInputStream(data)) {
			return store(in, fileName, null);
		}
	}

	public FileInfo store(InputStream in, String fileName) throws IOException {
		return store(in, fileName, null);
	}

	public FileInfo store(InputStream in, String fileName, String mimeType) throws IOException {
		FileInfo info = new FileInfo();
		info.fileId = UUID.randomUUID().toString().replaceAll("-", "");
		info.fileName = fileName;
		info.mimeType = (mimeType == null ? MimeUtils.getFileMime(info.getExtName()) : mimeType);
		info.createTime = new Date().getTime();

        String filePath = fileIdToPath(info.fileId);
		String dataFile = filePath + ".data";
        // Make sure all parent folders are exist.
        File dir = new File(dataFile).getParentFile();
        Files.createDirectories(dir.toPath());

		try (BufferedOutputStream bos = new BufferedOutputStream(
				new FileOutputStream(dataFile))) {
			int length;
			byte[] buffer = new byte[4096];
			while ((length = in.read(buffer)) != -1) {
				bos.write(buffer, 0, length);
			}
		}
        if (storeDetail) {
            String jsonFile = filePath + ".json";
            Serializer.writeJsonFile(info, jsonFile);
        }
		return info;
	}

    private static long getCreationTime(File f) throws IOException {
        BasicFileAttributes attributes = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
        return attributes.creationTime().toMillis();
    }

    private void listFiles(File folder, List<FileBasicInfo> files) throws IOException {
        File[] subFiles = folder.listFiles();
        if (subFiles == null)
            return;
        for (File f: subFiles) {
            if (f.isDirectory()) {
                listFiles(f, files);
            } else if (f.isFile() && f.getName().matches(".+\\.data")) {
                FileBasicInfo info = new FileBasicInfo();
                info.fileId = filePathToId(f);
                info.createTime = getCreationTime(f);
                files.add(info);
            }
        }
    }

	public List<FileBasicInfo> listFiles() throws IOException {
		File dir = new File(uploadDir);
        List<FileBasicInfo> files = new LinkedList<>();
        listFiles(dir, files);
        return files;
	}

    /**
     * Delete child folder if it's empty.
     * @param folder Folder file
     * @return Return true if folder is empty
     */
    private static boolean emptyFolder(File folder) {
        boolean isEmpty = true;
        File[] files = folder.listFiles();
        if (files == null)
            return true;
        for (File f: files) {
            if (f.isFile())
                isEmpty = false;
            else if (f.isDirectory()) {
                if (emptyFolder(f))
                    f.delete();
                else
                    isEmpty = false;
            }
        }
        return isEmpty;
    }

    /**
     * Keep upload directory clean, delete any empty child folders.
     */
    public void deleteEmptyFolder() {
        File dir = new File(uploadDir);
        emptyFolder(dir);
    }
	
	/**
	 * Delete old files which if currentTime - file.createTime &gt; timeInterval
	 * @param expiredMinutes in minute
	 * @throws java.io.IOException When cannot delete files
	 */
	public void deleteExpired(long expiredMinutes) throws IOException {
		List<FileBasicInfo> list = listFiles();
		long now = new Date().getTime();
		for (FileBasicInfo info: list) {
			if (now - info.createTime > expiredMinutes * 60 * 1000)
				deleteFile(info.fileId);
		}
	}
	
	public List<FileInfo> saveUploadFiles(HttpServletRequest request) throws ServletException, IOException, FileUploadException {
		return this.saveUploadFiles(request, maxUploadSize);
	}

	/**
	 * Save upload file to disk
	 * @param request HttpServletRequest
	 * @param maxSize maximum size of the upload file, in bytes.
	 * @return Saved file info list
	 * @throws ServletException
	 * @throws IOException
	 * @throws FileUploadException
	 */
	public List<FileInfo> saveUploadFiles(HttpServletRequest request, int maxSize)
			throws ServletException, IOException, FileUploadException {
		List<FileInfo> uploadList = new LinkedList<>();
		request.setCharacterEncoding("utf-8");
		ServletFileUpload upload = new ServletFileUpload();
		upload.setHeaderEncoding("UTF-8");
		if (!ServletFileUpload.isMultipartContent(request)) {
			return uploadList;
		}
		upload.setSizeMax(maxSize);
		FileItemIterator iterator = upload.getItemIterator(request);
		while (iterator.hasNext()) {
			FileItemStream item = iterator.next();
			try (InputStream stream = item.openStream()) {
				if (!item.isFormField()) {
					FileInfo info = new FileInfo();
					info.setFileName(item.getName());
					if (pattern != null && !pattern.matcher(info.fileName).matches()) {
						continue;
					}
					info = store(stream, info.fileName);
					uploadList.add(info);
				}
			}
		}
		return uploadList;
	}

	public FileInfo getFileInfo(String fileId) throws IOException {
        String filePath = fileIdToPath(fileId);
		File dataFile = new File(filePath + ".data");
		if (!dataFile.exists()) {
			return null;
		}
        File jsonFile = new File(filePath + ".json");
        if (jsonFile.exists())
            return Serializer.readJsonFile(jsonFile, FileInfo.class);
        else {
            FileInfo fileInfo = new FileInfo();
            fileInfo.fileId = fileId;
            fileInfo.createTime = getCreationTime(dataFile);
            fileInfo.fileName = fileId + ".data";
            fileInfo.mimeType = MimeUtils.UNKNOWN_MIME;
            return fileInfo;
        }
	}

	public String getFilePath(String fileId) {
        String filePath = fileIdToPath(fileId);
        filePath += ".data";
        if (new File(filePath).exists()) {
            return filePath;
        } else
            return null;
	}

	public InputStream openFile(String fileId) throws FileNotFoundException {
        String filePath = fileIdToPath(fileId) + ".data";
		return new FileInputStream(new File(filePath));
	}

    public void downloadFile(String fileId, String fileName, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        downloadFile(fileId, fileName, null, request, response);
    }

    public void downloadFile(String fileId, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        downloadFile(fileId, null, null, request, response);
    }

	/**
	 * Download file by specified file id
	 *
	 * @param response Servlet response
	 * @param fileName
	 * @param mimeType
	 * @param request Pass request parameter to provide user agent information
	 * to properly process utf-8 filename
	 * @param fileId File ID which generated by uploaded or manually created
	 * file
	 * @throws ServletException When send response failed
	 * @throws java.io.IOException When cannot download file
	 */
	public void downloadFile(String fileId, String fileName, String mimeType,
                             HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setCharacterEncoding("utf-8");
		FileInfo info = getFileInfo(fileId);
        if (info == null) {
            ServletUtils.send(response, HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String filePath = fileIdToPath(fileId);
        File dataFile = new File(filePath + ".data");
        if (fileName == null)
            fileName = info.fileName;
        if (mimeType == null)
            mimeType = info.mimeType;
		ServletUtils.download(request, response, dataFile, fileName, mimeType);
	}


}
