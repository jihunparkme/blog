package com.example.demo.review.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.review.entity.Review;
import com.example.demo.review.service.ReviewServiceImpl;

@Controller
public class ReviewController {

	@Autowired private ReviewServiceImpl reviewService;
	
	@GetMapping("/reviews")
	public String list(
			@PageableDefault(page = 0, size = 10) Pageable pageable,
			Model model) {
		
		Page<Review> reviewListPage = reviewService.getReviewRepository().findAll(pageable);
		
		model.addAttribute("galleryListPage", reviewListPage);
		
		return "reviews/listReview";
	}
	
}
