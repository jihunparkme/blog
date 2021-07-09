package com.example.demo.common.web;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.example.demo.util.file.FileUtilities;

@Controller
@RequestMapping("/common")
public class CKEditorController {

	/**
	 * CKEditor에서 파일 선택 후 서버로 전송 시 처리
	 * 
	 * @param response
	 * @param multipartRequest
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/ckeditor/fileUpload")
	public String fileUploadFromCKEditor(HttpServletResponse response, MultipartHttpServletRequest multipartRequest) throws Exception {

		PrintWriter printWriter = null;

		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		try {
			String fileName = FileUtilities.uploadCKEditorFile(multipartRequest.getFile("upload"), "files/ckeditor");
			String fileUrl = "/common/ckeditor/fileDownload?fileName=" + fileName; 
			
			printWriter = response.getWriter();
			// 서버로 파일 전송 후 이미지 정보 확인을 위해 filename, uploaded, fileUrl 정보를 response 해주어야 함
			printWriter.println("{\"filename\" : \"" + fileName + "\", \"uploaded\" : 1, \"url\":\"" + fileUrl + "\"}");
			printWriter.flush();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (printWriter != null) {
				printWriter.close();
			}
		}

		return null;
	}

	/**
	 * 파일 업로드 후 이미지 정보, Editor 화면에 이미지 출력 처리
	 * 
	 * @param fileName
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping("/ckeditor/fileDownload")
    public void ckSubmit(@RequestParam(value="fileName") String fileName,
                            HttpServletRequest request, HttpServletResponse response) {
    
		File file = FileUtilities.getDownloadFile(fileName, "files/ckeditor");

		try {
			byte[] data = FileUtils.readFileToByteArray(file);
			
			response.setContentType(FileUtilities.getMediaType(fileName).toString());
			response.setContentLength(data.length);
			response.setHeader("Content-Transfer-Encoding", "binary");
			response.setHeader("Content-Disposition", "attachment; fileName=\"" + URLEncoder.encode(fileName, "UTF-8") + "\";");

			response.getOutputStream().write(data);
			response.getOutputStream().flush();
			response.getOutputStream().close();

		} catch (IOException e) {
			throw new RuntimeException("파일 다운로드에 실패하였습니다.");
		} catch (Exception e) {
			throw new RuntimeException("시스템에 문제가 발생하였습니다.");
		}
    }
}
