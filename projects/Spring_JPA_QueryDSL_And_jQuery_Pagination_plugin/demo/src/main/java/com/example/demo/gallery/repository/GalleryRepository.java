package com.example.demo.gallery.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.gallery.entity.Gallery;
import com.example.demo.gallery.repository.impl.GalleryRepositoryCustom;

public interface GalleryRepository extends JpaRepository<Gallery, Long>, GalleryRepositoryCustom {

}
