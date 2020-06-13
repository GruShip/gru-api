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

@Entity
@Table(name = "productinventory")
public class ProductInventory extends BaseEntity{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", columnDefinition = "bigint not null auto_increment")
	private Long id;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "seller_product_id", columnDefinition = "bigint")
	private SellerProduct sellerProduct;
	
	@Column(name = "operation_type")
	private String operationType;
	
	@Column(name = "quantity")
	private Integer quantity;
	
	@Column(name = "total_purchase_price")
	private Double totalPurchasePrice;
	
	@Column(name = "aw_bill_number")
	private String awBillNumber;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public SellerProduct getSellerProduct() {
		return sellerProduct;
	}
	public void setSellerProduct(SellerProduct sellerProduct) {
		this.sellerProduct = sellerProduct;
	}
	public String getOperationType() {
		return operationType;
	}
	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	public Double getTotalPurchasePrice() {
		return totalPurchasePrice;
	}
	public void setTotalPurchasePrice(Double totalPurchasePrice) {
		this.totalPurchasePrice = totalPurchasePrice;
	}
	public String getAwBillNumber() {
		return awBillNumber;
	}
	public void setAwBillNumber(String awBillNumber) {
		this.awBillNumber = awBillNumber;
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
