package com.tech.dream.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ProductTypeDTO {
	
	private Long id;
	private String name;
	private String desc;
	private Boolean active;
	private Long productBrandId;
	private String productBrandName;

}
