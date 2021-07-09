package com.example.demo.util.file;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.gallery.entity.Attachments;
import com.example.demo.gallery.exception.FileException;

public class FileUtilities {

	// Paths.get()으로 운영체제에 따라서 다른 파일구분자 처리 
	public final static String rootPath = Paths.get("C:", "Users", "jihun.park", "Desktop", "testFile").toString();
	
	/**
	 * MultipartFile 형태의 파일을 Attachments Entity 형태로 파싱
	 * 
	 * @param multipartFiles
	 * @param gallery
	 */
	public static List<Attachments> parseFileInfo(List<MultipartFile> multipartFiles, String subPath) throws Exception {

		// 파일이 첨부되지 않았을 경우
		if (CollectionUtils.isEmpty(multipartFiles)) {
			return Collections.emptyList();
		}
		
		// 파일 업로드 경로 생성
		String savePath = Paths.get(rootPath, subPath).toString();
		verifyUploadPath(savePath);

		List<Attachments> fileList = new ArrayList<>();

		for (MultipartFile multipartFile : multipartFiles) {

			String origFilename = multipartFile.getOriginalFilename();
			if (origFilename == null || "".equals(origFilename)) continue;
			
			String filename = getUuidFileName(origFilename);
			String filePath = Paths.get(savePath, filename).toString();

			Attachments attachments = new Attachments(multipartFile.getOriginalFilename(), 
													filename, 
													filePath, 
													multipartFile.getSize());

			fileList.add(attachments);

			try {
				File file = new File(filePath);
				
				// 파일 권한 설정(쓰기, 읽기)
				file.setWritable(true);
				file.setReadable(true);
				
				multipartFile.transferTo(file);

			} catch (IOException e) {
				throw new FileException("[" + multipartFile.getOriginalFilename() + "] failed to save file...");

			} catch (Exception e) {
				throw new FileException("[" + multipartFile.getOriginalFilename() + "] failed to save file...");
			}
		}
		
		return fileList;
	}
	
	public static String uploadCKEditorFile(MultipartFile multipartFile, String subPath) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		
		// 파일 업로드 경로 생성
		String savePath = Paths.get(rootPath, subPath).toString();
		verifyUploadPath(savePath);
		
		String origFilename = multipartFile.getOriginalFilename();
		if (origFilename == null || "".equals(origFilename)) return null;
		
		String filename = getUuidFileName(origFilename);
		String filePath = Paths.get(savePath, filename).toString();

		try {
			File file = new File(filePath);
			
			// 파일 권한 설정(쓰기, 읽기)
			file.setWritable(true);
			file.setReadable(true);
			
			multipartFile.transferTo(file);
			
		} catch (IOException e) {
			throw new FileException("[" + multipartFile.getOriginalFilename() + "] failed to save file...");

		} catch (Exception e) {
			throw new FileException("[" + multipartFile.getOriginalFilename() + "] failed to save file...");
		}
		
		return filename;
	}
	
	private static void verifyUploadPath(String path) {

		if (!new File(path).exists()) {
			try {
				new File(path).mkdir();
			} catch (Exception e) {
				e.getStackTrace();
			}
		}
	}

	/**
	 * 다운로드 받을 파일 생성
	 * 
	 * @param attach
	 */
	public static File getDownloadFile(String filaName, String subPath) {

		return new File(Paths.get(rootPath, subPath).toString(), filaName);
	}
	
	/**
	 * 파일명 중복 방지를 위해 UUID 파일명 생성
	 * 
	 * @param filename
	 * @return
	 */
	public static String getUuidFileName(String filename) {
		
		UUID uuid = UUID.randomUUID();
		StringBuilder sb = new StringBuilder();
		sb.append(FilenameUtils.getBaseName(filename))
			.append("_").append(uuid).append(".").append(FilenameUtils.getExtension(filename));
		
		return  sb.toString();
	}
	
	/**
	 * 파일명 중복 방지를 위해 MD5(128비트 암호화 해시 함수) 파일명 생성
	 * 
	 * @param input
	 */
	public static String MD5Generator(String input) throws UnsupportedEncodingException, NoSuchAlgorithmException {
    	
        MessageDigest mdMD5 = MessageDigest.getInstance("MD5");
        mdMD5.update(input.getBytes("UTF-8"));
        
        byte[] md5Hash = mdMD5.digest();
        StringBuilder hexMD5hash = new StringBuilder();
        
        for(byte b : md5Hash) {
            String hexString = String.format("%02x", b);
            hexMD5hash.append(hexString);
        }
        
        return hexMD5hash.toString();
    }
	
	/**
	 * MediaType 생성
	 *  
	 * @param filename
	 */
	public static MediaType getMediaType(String filename) {
		
		String contentType = FilenameUtils.getExtension(filename);
		MediaType mediaType = null;
		
		if (contentType.equals("png")) {
			mediaType = MediaType.IMAGE_PNG; 
		} else if (contentType.equals("jpeg") || contentType.equals("jpg")) {
			mediaType = MediaType.IMAGE_JPEG;
		} else if (contentType.equals("gif")) {
			mediaType = MediaType.IMAGE_GIF;
		} else {
			mediaType = MediaType.APPLICATION_OCTET_STREAM;
		}
		
		return mediaType;
	}
}
