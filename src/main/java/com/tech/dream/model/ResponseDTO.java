package com.tech.dream.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ResponseDTO<T, E> {

	T data;
	private String status;
	private String message;
	private Long totalCount;
	private Long pageNumber;
    private Long pageSize;
	E error;

	public ResponseDTO(String status, T data, E error, Long totalCount, Long pageNumber, Long pageSize) {
		this.status = status;
		this.data = data;
		this.error = error;
		this.totalCount = totalCount;
		this.pageNumber = pageNumber; 
		this.pageSize = pageSize;
	}
}
