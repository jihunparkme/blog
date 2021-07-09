package com.example.demo.review.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.review.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	Page<Review> findAllByOrderByIdDesc(Pageable pageable);
	
}
