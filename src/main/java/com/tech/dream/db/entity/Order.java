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
@Table(name = "`order`")
public class Order extends BaseEntity{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", columnDefinition = "bigint not null auto_increment")
	private Long id;
	
	@Column(name = "order_number")
	private String orderNumber;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "order_batch_id", columnDefinition = "bigint")
	private OrderBatch orderBatch;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "seller_product_id", columnDefinition = "bigint")
	private SellerProduct sellerProduct;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "buyer_company_branch_id", columnDefinition = "bigint")
	private CompanyBranch buyerCompanyBranch;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "buyer_address_id", columnDefinition = "bigint")
	private Address buyerAddress;
	
	@Column(name = "`status`")
	private String status;
	
	@Column(name = "quantity")
	private Integer quantity;
	
	@Column(name = "price")
	private Double price;
	
	@Column(name = "tax")
	private Double tax;
	
	@Column(name = "total_price")
	private Double totalPrice;
	
	@PrePersist
    protected void onCreate() {
		super.onCreate();
	}

	@PreUpdate
    protected void onUpdate() {
		super.onUpdate();
	}
	
}
