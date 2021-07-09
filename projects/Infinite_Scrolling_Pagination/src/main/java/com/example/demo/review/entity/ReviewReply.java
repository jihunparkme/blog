package com.example.demo.review.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.example.demo.gallery.domain.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity(name="ReviewReply")
@Table(name="REVIEW_REPLY")
public class ReviewReply extends BaseTimeEntity {

	@Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="REPLY_ID")
    private Long id;

	@ManyToOne
	@JoinColumn(name = "REVIEW_ID", nullable = false)
    private Review review;

	@Column(name = "NAME", nullable = false)
    private String name;
	
	@Column(name="PASSWORD", nullable = false)
    private Long password;
	
	@Column(name="CONTENTS", nullable = false)
    private String contents;
	
	@Column(name="SECRET", nullable = false)
    private Boolean secret;
}

