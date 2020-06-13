package com.tech.dream.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "sellerproduct")
public class SellerProduct extends BaseEntity{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", columnDefinition = "bigint not null auto_increment")
	private Long id;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "product_id", columnDefinition = "bigint")
	private Product product;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "company_branch_id", columnDefinition = "bigint")
	private CompanyBranch companyBranch;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "`desc`")
	private String desc;
	
	@Column(name = "mrp_sell_price")
	private Double mrpSellPrice;
	
	@Column(name = "dealer_sell_price")
	private Double dealerSellPrice;
	
	@Column(name = "wholesale_sell_price")
	private Double wholesaleSellPrice;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "mrp_product_coupon_id", columnDefinition = "bigint")
	private ProductCoupon mrpProductCoupon;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "dealer_product_coupon_id", columnDefinition = "bigint")
	private ProductCoupon dealerProductCoupon;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "wholesale_product_coupon_id", columnDefinition = "bigint")
	private ProductCoupon wholesaleProductCoupon;
	
	@Column(name = "available_qty")
	private Integer availableQty;
	
	@Column(name = "is_approved")
	private Boolean isApproved;
	
	@Column(name = "tax_percentage")
	private Double taxPercentage;

	public SellerProduct(Long id) {
		this.id = id;
	}
	
	@PrePersist
    protected void onCreate() {
		super.onCreate();
	}

	@PreUpdate
    protected void onUpdate() {
		super.onUpdate();
	}
	
}
