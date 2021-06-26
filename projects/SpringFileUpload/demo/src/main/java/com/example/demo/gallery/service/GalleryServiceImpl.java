package com.example.demo.gallery.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.gallery.domain.GalleryDto;
import com.example.demo.gallery.entity.Attachments;
import com.example.demo.gallery.entity.Gallery;
import com.example.demo.gallery.repository.AttachmentsRepository;
import com.example.demo.gallery.repository.GalleryRepository;
import com.example.demo.gallery.util.FileUtilities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Getter
public class GalleryServiceImpl {

	private final GalleryRepository galleryRepository; 
	private final AttachmentsRepository attachmentsRepository;
	
    @Transactional(readOnly = true)
    public GalleryDto.Response searchById(Long id) {
       
    	Gallery entity = galleryRepository.findById(id).orElseThrow(()
                -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));
                
        return new GalleryDto.Response(entity);
    }
    
    @Transactional(readOnly = true)
    public List<GalleryDto.ListResponse> searchAllDesc() {
       
    	return galleryRepository.findAllByOrderByIdDesc().stream()
                .map(GalleryDto.ListResponse::new)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void delete(Long id){
    	
    	Gallery gallery = galleryRepository.findById(id)
                .orElseThrow(()->new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));

    	galleryRepository.delete(gallery);
    }
    
    
	@Transactional
	public Long save(Gallery gallery, List<MultipartFile> files, List<Long> deleteFileList) throws Exception {

		Gallery saveGallery = getGalleryRepository().save(gallery);
		
		List<Attachments> AttachmentsList = FileUtilities.parseFileInfo(files, saveGallery);
		
		// 파일이 존재할 경우
		if (!AttachmentsList.isEmpty()) {
			AttachmentsList.forEach(attachments -> attachmentsRepository.save(attachments));
		}
		
		// 삭제할 파일이 존재할 경우
		if (!deleteFileList.isEmpty()) {
			attachmentsRepository.deleteByAttachIdList(deleteFileList);
		}
		
		return saveGallery.getId();
	}
}