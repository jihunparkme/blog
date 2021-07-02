package com.example.demo.gallery.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.gallery.entity.Member;
import com.example.demo.gallery.repository.MemberRepository;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Getter
public class MemberServiceImpl {

	private final MemberRepository memberRepository;
	
    @Transactional(readOnly = true)
    public Member searchByName(String name) {
    	
    	Member member = memberRepository.findByName(name).orElseThrow(()
    			-> new IllegalArgumentException("존재하지 않는 회원입니다."));
    	
    	return member;
    }
}
