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
@Table(name = "producttype")
public class ProductType extends BaseEntity{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", columnDefinition = "bigint not null auto_increment")
	private Long id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "`desc`")
	private String desc;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "product_brand_id", columnDefinition = "bigint")
	private ProductBrand productBrand;
	
	public ProductType() {}
	public ProductType(Long id) {
		this.id = id;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public ProductBrand getProductBrand() {
		return productBrand;
	}
	public void setProductBrand(ProductBrand productBrand) {
		this.productBrand = productBrand;
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
