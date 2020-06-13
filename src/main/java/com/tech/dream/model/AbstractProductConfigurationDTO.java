package com.tech.dream.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class AbstractProductConfigurationDTO {

	private Long productCategoryId;
	private String productCategoryName;
	private Long productSubCategoryId;
	private String productSubCategoryName;
	
	private Long productBrandId;
	private String productBrandName;
	private String productBrandCompanyName;
	
	private Long productTypeId;
	private String productTypeName;
	
	private Long productModelId;
	private String productModelName;
	
	private Long productColorId;
	private String productColorName;
	
	private Long productMemoryId;
	private String productMemoryName;
	
	private Long productStorageId;
	private String productStorageName;
	
	private Long productScreenSizeId;
	private String productScreenSizeName;

	private Long productTaxRateId;
	private Double productTaxRatePercentage;
}
