package com.tech.dream.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class OrderDTO extends AbstractProductConfigurationDTO{
	private Long id;

	private String orderType;
	private String desc;

	private AddressDTO buyerAddress;
	private Integer quantity;
	private Double price;
	private Double tax;
	private Double totalPrice;
	private String status;
	
	private String orderNumber;
	private String statusDesc;
	
	private Long buyerCompanyBranchId;
	private String buyerCompanyBranchName;
	private Long buyerCompanyId;
	private String buyerCompanyName;
	
	private Long productId;
	private String productModelNumber;
	private String productDesc;
	private Double taxPercentage;
	
	private Long sellerProductId;
	private String sellerProductName;
	private String sellerProductDesc;
	private Long sellerCompanyBranchId;
	private String sellerCompanyBranchName;
	private Long sellerCompanyId;
	private String sellerCompanyName;
	
	private List<AssetDTO> assets;
	
	
}
