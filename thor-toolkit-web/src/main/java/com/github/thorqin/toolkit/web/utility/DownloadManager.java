/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.thorqin.toolkit.web.utility;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.github.thorqin.toolkit.utility.Serializer;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 *
 * @author nuo.qin
 */
public final class DownloadManager {

	private final static Logger logger = Logger.getLogger(DownloadManager.class.getName());
	private final static int maxSize = 1024 * 1024 * 5;
	private final static String unknowMimeType = "application/octet-stream";
	private final String uploadDir;
	private final Map<String, String> mime = new HashMap<String, String>() {
		private static final long serialVersionUID = 0L;

		{
			put("txt", "text/plain");
			put("jpeg", "image/jpeg");
			put("jpg", "image/jpeg");
			put("png", "image/png");
			put("gif", "image/gif");
			put("pdf", "application/pdf");
			put("xml", "text/xml");
			put("doc", "application/vnd.ms-word");
			put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
			put("docm", "application/vnd.ms-word.document.macroEnabled.12");
			put("xls", "application/vnd.ms-excel");
			put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			put("xlsm", "application/vnd.ms-excel.sheet.macroEnabled.12");
			put("ppt", "application/vnd.ms-powerpoint");
			put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
			put("ppat", "application/vnd.ms-powerpoint.presentation.macroEnabled.12");
		}
	};

	public DownloadManager(File baseDir) {
		this.uploadDir = baseDir.getAbsolutePath();
	}

	public void addMime(String suffix, String mimeType) {
		mime.put(suffix, mimeType);
	}

	public void removeMime(String suffix) {
		mime.remove(suffix);
	}

	public void clearMime() {
		mime.clear();
	}

	public static class FileInfo {

		public String fileId;
		public String fileName;
		public String mimeType;
		public long createTime;

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

	public void deleteFile(String fileId) {
		File jsonFile = new File(uploadDir + "/" + fileId + ".json");
		if (jsonFile.exists()) {
			jsonFile.delete();
		}
		File dataFile = new File(uploadDir + "/" + fileId + ".data");
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
		File dataFile = new File(uploadDir + "/" + fileId + ".data");
		if (replaceExisting)
			Files.move(dataFile.toPath(), destPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
		else
			Files.move(dataFile.toPath(), destPath.toPath());
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
		File dataFile = new File(uploadDir + "/" + fileId + ".data");
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
		info.mimeType = (mimeType == null ? getFileMIME(info.getExtName()) : mimeType);
		info.createTime = new Date().getTime();
		File dir = new File(uploadDir);
		dir.mkdir();

		String jsonFile = uploadDir + "/" + info.fileId + ".json";
		Serializer.saveJsonFile(info, jsonFile);
		String dataFile = uploadDir + "/" + info.fileId + ".data";
		try (BufferedOutputStream bos = new BufferedOutputStream(
				new FileOutputStream(dataFile))) {
			int length;
			byte[] buffer = new byte[4096];
			while ((length = in.read(buffer)) != -1) {
				bos.write(buffer, 0, length);
			}
		}
		return info;
	}

	public List<FileInfo> listFiles() throws IOException {
		File dir = new File(uploadDir);
		File[] array = dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().matches(".+\\.json$");
			}
		});
		List<FileInfo> list = new LinkedList<>();
		for (File f: array) {
			FileInfo info = Serializer.loadJsonFile(f, FileInfo.class);
			list.add(info);
		}
		return list;
	}
	
	/**
	 * Delete old files which if currentTime - file.createTime &gt; timeInterval
	 * @param expiredMinutes in minute
	 * @throws java.io.IOException When cannot delete files
	 */
	public void deleteExpired(long expiredMinutes) throws IOException {
		List<FileInfo> list = listFiles();
		long now = new Date().getTime();
		for (FileInfo info: list) {
			if (now - info.createTime > expiredMinutes * 1000 * 60)
				deleteFile(info.fileId);
		}
	}
	
	public List<FileInfo> saveUploadFiles(HttpServletRequest request) throws ServletException, IOException, FileUploadException {
		return this.saveUploadFiles(request, maxSize);
	}

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
		FileItemIterator iter;
		iter = upload.getItemIterator(request);
		while (iter.hasNext()) {
			FileItemStream item = iter.next();
			try (InputStream stream = item.openStream()) {
				if (!item.isFormField()) {
					FileInfo info = new FileInfo();
					info.setFileName(item.getName());
					if (getFileMIME(info.getExtName()) == null) {
						logger.log(Level.WARNING, "Upload file's MIME type isn't permitted.");
						continue;
					}
					info = store(stream, info.fileName);
					uploadList.add(info);
				}
			}
		}
		return uploadList;
	}

	public FileInfo getFileInfo(String fileId) {
		File dataFile = new File(uploadDir + "/" + fileId + ".data");
		if (!dataFile.exists()) {
			return null;
		}
		try {
			String jsonFile = uploadDir + "/" + fileId + ".json";
			return Serializer.loadJsonFile(jsonFile, FileInfo.class);
		} catch (Exception ex) {
			return null;
		}
	}

	public String getFilePath(String fileId) {
		File dataFile = new File(uploadDir + "/" + fileId + ".data");
		if (!dataFile.exists()) {
			return null;
		} else {
			return dataFile.getAbsolutePath();
		}
	}

	public InputStream openFile(String fileId) throws FileNotFoundException {
		File dataFile = new File(uploadDir + "/" + fileId + ".data");
		return new FileInputStream(dataFile);
	}

	/**
	 * Download file by specified file id
	 *
	 * @param response Servlet response
	 * @param request Pass request parameter to provide user agent information
	 * to properly process utf-8 filename
	 * @param fileId File ID which generated by uploaded or manually created
	 * file
	 * @throws ServletException When send response failed
	 * @throws java.io.IOException When cannot download file
	 */
	public void downloadFile(String fileId, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setCharacterEncoding("utf-8");

		File dataFile = new File(uploadDir + "/" + fileId + ".data");
		if (!dataFile.exists()) {
			ServletUtils.send(response, HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		FileInfo info;
		try {
			String jsonFile = uploadDir + "/" + fileId + ".json";
			info = Serializer.loadJsonFile(jsonFile, FileInfo.class);
		} catch (Exception ex) {
			ServletUtils.send(response, HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		ServletUtils.download(request, response, dataFile, info.fileName);
	}

	private String getFileMIME(String ext) {
		if (mime.containsKey(ext.toLowerCase())) {
			return mime.get(ext.toLowerCase());
		} else {
			return null;
		}
	}

}
