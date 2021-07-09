package com.example.demo.gallery.entity;

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
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity(name="Gallery")
@Table(name="GALLERY")
public class Gallery extends BaseTimeEntity {

	@Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="GALLERY_ID")
    private Long id;

	@Column(name = "MEMBER_NAME", nullable = false, updatable = false)
    private String memberName;

	@Column(name="MAIN_IMAGE_ID", nullable = true)
    private Long mainImageId;
	
	@Column(nullable = false)
    private String title;
	
	@Column(nullable = false)
    private String contents;
	
	@JsonIgnore
	@OneToMany(mappedBy="gallery", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
	private List<Attachments> attachmentsList = new ArrayList<>();
}
