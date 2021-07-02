package com.example.demo.gallery.domain;

import java.time.LocalDateTime;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;

/**
 * 모든 Entity의 상위 클래스
 * Entity들의 생성시간, 수정시간을 자동으로 관리
 * 
 * @MappedSuperclass : 상속 클래스들이 부모 클래스의 컬럼을 인식하도록 설정
 * @EntityListeners(AuditingEntityListener.class) : Auditing 기능
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {

    @CreatedDate
    private LocalDateTime createdDateTime;

    @LastModifiedDate
    private LocalDateTime modifiedDateTime;
}