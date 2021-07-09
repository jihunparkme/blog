package com.example.demo.review.service;

import org.springframework.stereotype.Service;

import com.example.demo.review.repository.ReviewRepository;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Getter
public class ReviewServiceImpl {

	private final ReviewRepository reviewRepository;
	
}
