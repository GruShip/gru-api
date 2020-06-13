package com.tech.dream.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
@Getter
@Setter
public class ProductInventoryItemDTO {
	
	private Long id;
	private Long sellerProductId;
	private String sellerProductName;
	private String imei1;
	private String imei2;
	private String itemId;
	
	private String createdDate;	

}
