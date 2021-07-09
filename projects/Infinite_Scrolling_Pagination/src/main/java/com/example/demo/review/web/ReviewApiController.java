package com.example.demo.review.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.review.entity.Review;
import com.example.demo.review.service.ReviewServiceImpl;
import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class ReviewApiController {

	@Autowired private ReviewServiceImpl reviewService;
	
	@PostMapping("/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public String save(Review review) throws Exception {

		reviewService.getReviewRepository().save(review);
		
		JsonObject jsonObject = new JsonObject();
		
		jsonObject.addProperty("response", "OK");
	        
        return jsonObject.toString();
    }
	
	@GetMapping("/reviews/list")
    public ResponseEntity<Page<Review>> list(
    		@PageableDefault(page = 0, size = 10) Pageable pageable) throws Exception {

		Pageable sortedByIdDesc = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("id").descending());
		Page<Review> reviewListPage = reviewService.getReviewRepository().findAllByOrderByIdDesc(sortedByIdDesc);
	        
		return new ResponseEntity<>(reviewListPage, HttpStatus.OK);
    }
	
	@DeleteMapping("/reviews")
    public String delete(@RequestParam("reviewId") Long reviewId, Model model){
    	
		reviewService.getReviewRepository().deleteById(reviewId);
    	
    	JsonObject jsonObject = new JsonObject();
    	jsonObject.addProperty("response", "OK");
    	
    	return jsonObject.toString();
    }
}
