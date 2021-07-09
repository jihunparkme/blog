package com.example.demo.gallery.entity;

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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity(name="Attachments")
@Table(name="ATTACHMENTS")
public class Attachments extends BaseTimeEntity {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ATTACHMENTS_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "GALLERY_ID")
    private Gallery gallery;

    @Column(nullable = false)
    private String origFileName;
    
    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath;

    private Long fileSize;

    @Builder
	public Attachments(String origFileName, String fileName, String filePath, Long fileSize) {
    	this.origFileName = origFileName;
		this.fileName = fileName;
		this.filePath = filePath;
		this.fileSize = fileSize;
	}
    
    public void setGallery(Gallery gallery) {
    	this.gallery = gallery;
    }
}