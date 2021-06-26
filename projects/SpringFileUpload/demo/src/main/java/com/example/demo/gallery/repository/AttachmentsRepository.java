
package com.example.demo.gallery.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.gallery.entity.Attachments;

public interface AttachmentsRepository extends JpaRepository<Attachments, Long> {

	/**
	 * Find attachments by the gallery ID
	 * 
	 * @param galleryId
	 * @return
	 */
	public List<Attachments> findAllByGalleryId(Long galleryId);
	
	/**
	 * Delete attachments by the delete file list
	 * 
	 * @param deleteFileList
	 */
	@Modifying
	@Query(value = "DELETE FROM Attachments a " + 
			"WHERE a.id IN (:deleteFileList)")
	public void deleteByAttachIdList(@Param("deleteFileList") List<Long> deleteFileList);
}
