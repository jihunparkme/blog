package com.example.demo.gallery.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.gallery.entity.Gallery;

public interface GalleryRepository extends JpaRepository<Gallery, Long> {

	List<Gallery> findAllByOrderByIdDesc();
	
}
