package com.example.responseApi.api.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.responseApi.api.dto.Member;
import com.example.responseApi.api.repository.MemberRepository;
import com.example.responseApi.api.response.BasicResponse;

import lombok.RequiredArgsConstructor;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class ApiController {

	private final MemberRepository memberRepository;

	@GetMapping("/{id}")
	public ResponseEntity<BasicResponse> find(@PathVariable Long id) {

		BasicResponse basicResponse = new BasicResponse();
		Optional<Member> member = memberRepository.findById(id);

		if (member.isPresent()) {
			basicResponse = BasicResponse.builder()
										.code(HttpStatus.OK.value())
										.httpStatus(HttpStatus.OK)
										.message("사용자 조회 성공")
										.result(Arrays.asList(member.get()))
										.count(1).build();

		} else {
			basicResponse = BasicResponse.builder()
										.code(HttpStatus.NOT_FOUND.value())
										.httpStatus(HttpStatus.NOT_FOUND)
										.message("사용자를 찾을 수 없습니다.")
										.result(Collections.emptyList())
										.count(0).build();
			
		}
		
		return new ResponseEntity<>(basicResponse, basicResponse.getHttpStatus());
	}

	@GetMapping("/all")
	public ResponseEntity<BasicResponse> list() {
		List<Member> memberList = memberRepository.findAll();

		BasicResponse basicResponse = BasicResponse.builder()
												.code(HttpStatus.OK.value())
												.httpStatus(HttpStatus.OK)
												.message("전체 사용자 조회 성공")
												.result(new ArrayList<>(memberList))
												.count(memberList.size()).build();
		
		return new ResponseEntity<>(basicResponse, HttpStatus.OK);
	}
}
