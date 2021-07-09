package com.example.demo.gallery.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.gallery.entity.Attachments;
import com.example.demo.gallery.entity.Gallery;
import com.example.demo.gallery.repository.AttachmentsRepository;
import com.example.demo.gallery.repository.GalleryRepository;
import com.example.demo.util.file.FileUtilities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Getter
public class GalleryServiceImpl {

	private final GalleryRepository galleryRepository; 
	private final AttachmentsRepository attachmentsRepository;
	
    @Transactional(readOnly = true)
    public Gallery searchById(Long id) {
       
    	Gallery gallery = galleryRepository.findById(id).orElseThrow(()
                -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));
                
        return gallery;
    }
    
    @Transactional
    public void delete(Long id){
    	
    	Gallery gallery = galleryRepository.findById(id)
                .orElseThrow(()->new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));

    	galleryRepository.delete(gallery);
    }
    
    
	@Transactional
	public Long save(Gallery gallery, List<MultipartFile> files, List<Long> deleteFileList) throws Exception {
		
		List<Attachments> attachmentsList = FileUtilities.parseFileInfo(files, "files");
		
		// 첨부파일 리스트 병합
		gallery.setAttachmentsList(
				Stream.concat(gallery.getAttachmentsList().stream(), attachmentsList.stream()).collect(Collectors.toList())
				);
		
		getGalleryRepository().save(gallery);
		
		// 첨부 파일이 존재할 경우
		if (!attachmentsList.isEmpty()) {
			attachmentsList.forEach(attachments -> {
				attachments.setGallery(gallery);
				attachmentsRepository.save(attachments);	
			});
		}
		
		// 등록일 경우 첫 번째 이미지를 Thumbnail로
		if (gallery.getMainImageId() == null) {
			gallery.setMainImageId(attachmentsList.get(0).getId());
			getGalleryRepository().save(gallery);
		} 
		
		// 삭제할 첨부 파일이 존재할 경우
		if (!deleteFileList.isEmpty()) {
			attachmentsRepository.deleteByAttachIdList(deleteFileList);
		}
		
		return gallery.getId();
	}
}