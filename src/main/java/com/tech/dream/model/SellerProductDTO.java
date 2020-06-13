package com.tech.dream.model;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SellerProductDTO {
	
	private Long id;
	private Long companyBranchId;
	private String companyBranchName;
	private Long companyId;
	private String companyName;
	private Long productId;
	private String productModelNumber;
	
	private String desc;
	private Integer availableQty;
	private Double mrpSellPrice;
	private Double dealerSellPrice;
	private Double wholesaleSellPrice;
	
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
	
	
	private Boolean active;
	private Boolean isApproved;
	private String name;
	private Double taxPercentage;
	
	private List<AssetDTO> productAssets;
	private List<AssetDTO> sellerProductAssets;

}
