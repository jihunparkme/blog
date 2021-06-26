package com.example.demo.gallery.web;

import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
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
import com.example.demo.gallery.entity.Gallery;
import com.example.demo.gallery.repository.impl.GalleryRepositoryCustomImpl.GalleryParam;
import com.example.demo.gallery.service.GalleryServiceImpl;
import com.example.demo.gallery.util.FileUtilities;

@Controller
@RequestMapping("/gallery")
public class GalleryController {
	
	@Autowired private GalleryServiceImpl galleryService;
	
	/**
	 * View Gallery List
	 * 
	 * @param searchType
	 * @param searchValue
	 * @param period
	 * @param fromDate
	 * @param toDate
	 * @param pageable
	 * @param model
	 * @return
	 */
	@GetMapping("/list")
	public String list(
			@RequestParam(value = "searchType", required = false, defaultValue = "ALL") String searchType,
			@RequestParam(value = "searchValue", required = false, defaultValue = "") String searchValue,
			@RequestParam(value = "period", required = false, defaultValue = "ALL") String period,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "fromDate", required = false) LocalDate fromDate,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "toDate", required = false) LocalDate toDate,
			@PageableDefault(page = 0, size = 10) Pageable pageable,
			Model model) {
		
		GalleryParam galleryParam = setGalleryParameter(searchType, searchValue, period, fromDate, toDate);
		
		Page<Gallery> galleryListPage = galleryService.getGalleryRepository().findAllGallery(galleryParam, pageable);
		
		model.addAttribute("galleryListPage", galleryListPage);
		
		return "gallery/listGallery";
	}

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
	 * View Gallery Write Page
	 * 
	 * @param model
	 * @return
	 */
	@GetMapping("/edit")
	public String write(Model model) {
		
		model.addAttribute("gallery", new GalleryDto.Response());
		
		return "gallery/editGallery";
	}
	
	/**
	 * View Gallery Edit Page
	 * 
	 * @param id
	 * @param model
	 * @return
	 */
	@GetMapping("/edit/{id}")
	public String edit(@PathVariable Long id, Model model) {
		
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
	
	/**
	 * Set Gallery Parameter for Search
	 * 
	 * @param searchType
	 * @param searchValue
	 * @param period
	 * @param fromDate
	 * @param toDate
	 * @return
	 */
	private GalleryParam setGalleryParameter(String searchType, String searchValue, 
			String period, LocalDate fromDate, LocalDate toDate) {
		
		GalleryParam galleryParam = new GalleryParam();

		galleryParam.setSearchType(searchType);
		galleryParam.setSearchValue(searchValue);
		galleryParam.setPeriod(period);
		if (fromDate != null && toDate != null) {
			galleryParam.setFromDate(fromDate.atStartOfDay());
			galleryParam.setToDate(toDate.plusDays(1L).atStartOfDay());
		}
		
		return galleryParam;
	}
}
