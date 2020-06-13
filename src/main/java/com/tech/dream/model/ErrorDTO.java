package com.tech.dream.model;

import org.springframework.http.HttpStatus;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ErrorDTO {
	
	private String message;
	private String stackTrace;
	private Integer code;
	private Integer index;
	private HttpStatus status;
	
	public ErrorDTO(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}
	
	public ErrorDTO(HttpStatus status, String message, String stackTrace) {
		this.status = status;
		this.message = message;
		this.stackTrace = stackTrace;
	}	
}
