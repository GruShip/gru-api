package com.tech.dream.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class ProductInventoryDTO {

	private Long id;
	private String operationType;
	private Integer quantity;
	private Double totalPurchasePrice;
	private String awBillNumber;
	private Long sellerProductId;
	private String sellerProductName;
	private String createdDate;	
}
