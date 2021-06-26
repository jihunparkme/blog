package com.example.demo.gallery.web;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.gallery.domain.GalleryDto;
import com.example.demo.gallery.entity.Attachments;
import com.example.demo.gallery.service.GalleryServiceImpl;
import com.example.demo.gallery.util.FileUtilities;

@Controller
@RequestMapping("/gallery")
public class GalleryController {
	
	@Autowired private GalleryServiceImpl galleryService;
	
	/**
	 * View Gallery
	 * 
	 * @param id
	 * @param model
	 * @return
	 */
	@GetMapping("/{id}")
    public String searchById(@PathVariable Long id, Model model) {
        
    	model.addAttribute("gallery", galleryService.searchById(id));
    	model.addAttribute("fileList", galleryService.getAttachmentsRepository().findAllByGalleryId(id));
    	
        return "gallery/viewGallery";
    }
	
	/**
	 * Move Gallery Edit Page
	 * 
	 * @param model
	 * @return
	 */
	@GetMapping("/edit")
	public String newGallery(Model model) {
		
		model.addAttribute("gallery", new GalleryDto.Response());
		
		return "gallery/editGallery";
	}
	
	/**
	 * Edit Gallery
	 * 
	 * @param id
	 * @param model
	 * @return
	 */
	@GetMapping("/edit/{id}")
	public String editGallery(@PathVariable Long id, Model model) {
		
		// 관리자 or 작성자가 아닐 경우 수정 불가 로직
		
		model.addAttribute("gallery", galleryService.searchById(id));
		model.addAttribute("fileList", galleryService.getAttachmentsRepository().findAllByGalleryId(id));
		
		return "gallery/editGallery";
	}
	
	/**
	 * Display Img
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@GetMapping(value = "/display")
	public ResponseEntity<byte[]> displayImgFile(@RequestParam("id") Long id)throws Exception{
		
		InputStream in = null;
		ResponseEntity<byte[]> entity = null;
		Optional<Attachments> optAttach = galleryService.getAttachmentsRepository().findById(id);
		if(!optAttach.isPresent()) {
			new RuntimeException("이미지 정보를 찾을 수 없습니다."); 
		}
		
		Attachments attach = optAttach.get(); 
		
		try {
			HttpHeaders headers = new HttpHeaders();
			in = new FileInputStream(attach.getFilePath());
			headers.setContentType(FileUtilities.getMediaType(attach.getOrigFileName()));
			headers.add("Content-Disposition", "attachment; filename=\"" + new String(attach.getOrigFileName().getBytes("UTF-8"), "ISO-8859-1")+"\""); 
			
			entity = new ResponseEntity<byte[]>(IOUtils.toByteArray(in), headers, HttpStatus.CREATED);
			
		} catch(Exception e) {
			e.printStackTrace();
			entity = new ResponseEntity<byte[]>(HttpStatus.BAD_REQUEST);
		} finally {
			in.close();
		}
		
		return entity;
	}
}
