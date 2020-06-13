package com.tech.dream.model;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductListingDTO extends AbstractProductConfigurationDTO{
	
	private Long sellerProductId;
	private String sellerProductName;
	private Double mrpSellPrice;
	private Double dealerSellPrice;
	private Double wholesaleSellPrice;
	private Long companyBranchId;
	private String companyBranchName;
	private Long companyId;
	private String companyName;
	private Long productId;
	private String productModelNumber;
	private String productDesc;
	private String sellerProductDesc;

	private Long mrpProductCouponId;
	private String mrpProductCouponName;
	private Double mrpProductCouponValue;
	private String mrpProductCouponDiscountType;
	
	private Long dealerProductCouponId;
	private String dealerProductCouponName;
	private Double dealerProductCouponValue;
	private String dealerProductCouponDiscountType;

	private Long wholesaleProductCouponId;
	private String wholesaleProductCouponName;
	private Double wholesaleProductCouponValue;
	private String wholesaleProductCouponDiscountType;
	
	private List<AssetDTO> assets;

}
