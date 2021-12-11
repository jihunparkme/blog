package com.example.responseApi.api.dto;

import lombok.Data;

@Data
public class Member {
	
	private Long id;
	private String name;
	private String dept;
	
	public Member(String name, String dept) {
		this.name = name;
		this.dept = dept;
	}
}
