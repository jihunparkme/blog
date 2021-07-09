package com.example.demo.review.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.example.demo.gallery.domain.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity(name="Review")
@Table(name="REVIEW")
public class Review extends BaseTimeEntity {

	@Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="REVIEW_ID")
    private Long id;

	@Column(name = "NAME", nullable = false)
    private String name;

	@Column(name="PASSWORD", nullable = false)
    private String password;
	
	@Column(name="CONTENTS", nullable = false)
    private String contents;
	
	@Column(name="SECRET", nullable = false)
    private Boolean secret;
	
	@OneToMany(mappedBy="review", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
	private List<ReviewReply> replyList = new ArrayList<>();
}
