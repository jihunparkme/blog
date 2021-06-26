package com.example.demo.gallery.repository.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.demo.gallery.entity.Gallery;
import com.example.demo.gallery.repository.impl.GalleryRepositoryCustomImpl.GalleryParam;

public interface GalleryRepositoryCustom {

	public Page<Gallery> findAllGallery(GalleryParam param, Pageable pageable);
	
	public List<Gallery> findAllGallery(GalleryParam param);
}
