package com.example.demo.gallery.web;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.example.demo.gallery.entity.Attachments;
import com.example.demo.gallery.entity.Gallery;
import com.example.demo.gallery.entity.Member;
import com.example.demo.gallery.service.GalleryServiceImpl;
import com.example.demo.gallery.service.MemberServiceImpl;
import com.example.demo.util.file.FileUtilities;
import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class GalleryApiController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GalleryApiController.class);
	
    private final GalleryServiceImpl galleryService;
    private final MemberServiceImpl memberService;

    /**
     * Save Gallery
     * 
     * @param multiRequest
     * @return
     * @throws Exception
     */
    @PostMapping("/gallery")
    @ResponseStatus(HttpStatus.CREATED)
    public String save(MultipartHttpServletRequest multiRequest) throws Exception {

    	LOGGER.debug(multiRequest.getParameter("id"));
    	JsonObject jsonObject = new JsonObject();
    	
    	// 작성자 정보 확인
    	Optional<Member> optMember = memberService.getMemberRepository().findByName(multiRequest.getParameter("memberName"));
    	if (!optMember.isPresent()) {
	        jsonObject.addProperty("response", "error");
	        jsonObject.addProperty("errorMsg", "사용자 정보를 찾을 수 없습니다.");
	        
	        return jsonObject.toString();
		}
    	
    	Gallery gallery = new Gallery();
    	List<Long> deleteFileList = new ArrayList<>();
    	
    	// 신규 등록
    	if (multiRequest.getParameter("id") == null) {
    		gallery.setMemberName(multiRequest.getParameter("memberName"));
    		gallery.setTitle(multiRequest.getParameter("title"));
    		gallery.setContents(multiRequest.getParameter("contents"));
    	}
    	// 수정
    	else {
    		Optional<Gallery> optGallery = galleryService.getGalleryRepository().findById(Long.parseLong(multiRequest.getParameter("id")));
    		if (!optGallery.isPresent()) {
    			jsonObject.addProperty("response", "error");
    	        jsonObject.addProperty("errorMsg", "게시물 정보를 찾을 수 없습니다.");
    	        
    	        return jsonObject.toString();
    		}
    		
    		gallery = optGallery.get();
    		gallery.setTitle(multiRequest.getParameter("title"));
    		gallery.setContents(multiRequest.getParameter("contents"));
    		gallery.setMainImageId(Long.parseLong(multiRequest.getParameter("thumbnail")));
    		
    		if (!"".equals(multiRequest.getParameter("deleteFiles"))) {
	    		deleteFileList = Arrays.asList(multiRequest.getParameter("deleteFiles").split(",")).stream()
	    									.map(s -> Long.parseLong((String) s)).collect(Collectors.toList());
    		}
    	}
    	
        Long id = galleryService.save(gallery, multiRequest.getFiles("files"), deleteFileList);
        
        jsonObject.addProperty("response", "OK");
        jsonObject.addProperty("galleryId", id);
        
        return jsonObject.toString();
    }
	
    /**
     * Delete Gallery
     * 
     * @param galleryId
     */
    @PostMapping("/gallery/delete")
    public String delete(@RequestParam("galleryId") Long galleryId, Model model){
    	
    	galleryService.delete(galleryId);
    	
    	JsonObject jsonObject = new JsonObject();
    	jsonObject.addProperty("response", "OK");
    	
    	return jsonObject.toString();
    }
    
    /**
     * Download Attachments
     * 
     * @param id
     * @param response
     */
    @GetMapping("/gallery/download/{id}")
    public void downloadAttach(@PathVariable Long id, HttpServletResponse response){
    	
    	Optional<Attachments> optAttach = galleryService.getAttachmentsRepository().findById(id);
    	if (!optAttach.isPresent()) {
    		throw new RuntimeException("파일을 찾을 수 없습니다.");
    	}
    	
    	Attachments attach = optAttach.get();
    	File file = FileUtilities.getDownloadFile(attach.getFileName(), "files");

		try {
			byte[] data = FileUtils.readFileToByteArray(file);
			
			response.setContentType(FileUtilities.getMediaType(attach.getOrigFileName()).toString());
			response.setContentLength(data.length);
			response.setHeader("Content-Transfer-Encoding", "binary");
			response.setHeader("Content-Disposition", "attachment; fileName=\"" + URLEncoder.encode(attach.getOrigFileName(), "UTF-8") + "\";");

			response.getOutputStream().write(data);
			response.getOutputStream().flush();
			response.getOutputStream().close();

		} catch (IOException e) {
			throw new RuntimeException("파일 다운로드에 실패하였습니다.");
		} catch (Exception e) {
			throw new RuntimeException("시스템에 문제가 발생하였습니다.");
		}
    }
    
	@GetMapping("/gallery/scroll/list")
	public ResponseEntity<List<Gallery>> scrollList(
			@PageableDefault(page = 0, size = 10) Pageable pageable, Model model) {
		
		// Repository 에 Paging 정보를 요청하기 위해 Pageable 객체 생성 (page, size, 정렬 정보)
		Pageable sortedByIdDesc = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("id").descending());
		Page<Gallery> galleryListPage = galleryService.getGalleryRepository().findAllByOrderByIdDesc(sortedByIdDesc);
		
		// List<Entity> 정보를 넘겨주기 위해 ResponseEntity 사용
		return new ResponseEntity<>(galleryListPage.getContent(), HttpStatus.OK);
	}
}
 