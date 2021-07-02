package com.example.demo.gallery.repository.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import com.example.demo.gallery.entity.Gallery;
import com.example.demo.gallery.entity.QGallery;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.JPQLQuery;

import lombok.Data;

public class GalleryRepositoryCustomImpl extends QuerydslRepositorySupport implements GalleryRepositoryCustom {

	public GalleryRepositoryCustomImpl() {
		super(Gallery.class);
	}

	@Data
	public static class GalleryParam {
		private String searchType;
		private String searchValue;
		private String period;
		private LocalDateTime fromDate;
		private LocalDateTime toDate;
	}
	
	@Override
	public Page<Gallery> findAllGallery(GalleryParam param, Pageable pageable) {

		JPQLQuery<Gallery> query = findAllGalleryQuery(param, pageable);
		
		QueryResults<Gallery> queryResults = query.fetchResults();
		
		return new PageImpl<Gallery>(queryResults.getResults(), pageable, queryResults.getTotal());
	}

	@Override
	public List<Gallery> findAllGallery(GalleryParam param) {
		
		JPQLQuery<Gallery> query = findAllGalleryQuery(param, null);
		
		return query.fetchResults().getResults();
	}

	private JPQLQuery<Gallery> findAllGalleryQuery(GalleryParam param, Pageable pageable) {
		
		QGallery gallery = QGallery.gallery;
		
		JPQLQuery<Gallery> query = from(gallery);
		
		BooleanBuilder bb = new BooleanBuilder();
		
		// period
		{
			if (param.getFromDate() != null && param.getToDate() != null) {
				bb.and(gallery.createdDateTime.between(param.getFromDate(), param.getToDate()));
			}
		}
		
		// searchType & searchValue
		{
			switch (param.getSearchType()) {
			case "ALL" :
				bb.and(gallery.title.contains(param.getSearchValue()).
						or(gallery.memberName.contains(param.getSearchValue())));
				break;
			case "TITLE" : 
				bb.and(gallery.title.contains(param.getSearchValue()));
				break;
			case "WRITER" :
				bb.and(gallery.memberName.contains(param.getSearchValue()));
				break;
			}
		}
		
		if (pageable != null) {
			query.limit(pageable.getPageSize());
			query.offset(pageable.getOffset());
		}
		
		OrderSpecifier<Long> orderId = gallery.id.desc();
		
		return query.distinct().where(bb).orderBy(orderId);
	}
}
