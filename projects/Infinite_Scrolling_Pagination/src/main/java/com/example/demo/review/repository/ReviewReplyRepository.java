package com.example.demo.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.review.entity.ReviewReply;

public interface ReviewReplyRepository extends JpaRepository<ReviewReply, Long> {

}
