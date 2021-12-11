package com.example.responseApi;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.example.responseApi.api.dto.Member;
import com.example.responseApi.api.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TestDataInit {

	private final MemberRepository memberRepository; 
	
	@PostConstruct
    public void init() {
		memberRepository.save(new Member("Cristoval", "back-end"));
		memberRepository.save(new Member("Aaron", "front-end"));
    }
}
