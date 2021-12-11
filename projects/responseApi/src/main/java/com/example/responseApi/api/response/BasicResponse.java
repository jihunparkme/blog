package com.example.responseApi.api.response;

import java.util.List;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder@AllArgsConstructor
@NoArgsConstructor
public class BasicResponse {
	
	private Integer code;
	private HttpStatus httpStatus;
	private String message;
	private Integer count;
	private List<Object> result;
}
