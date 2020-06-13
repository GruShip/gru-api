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
@Table(name = "product")
public class Product extends BaseEntity{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", columnDefinition = "bigint not null auto_increment")
	private Long id;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "product_sub_category_id", columnDefinition = "bigint")
	private ProductSubCategory productSubCategory;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "product_model_id", columnDefinition = "bigint")
	private ProductModel productModel;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "product_color_id", columnDefinition = "bigint")
	private ProductColor productColor;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "product_memory_id", columnDefinition = "bigint")
	private ProductMemory productMemory;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "product_storage_id", columnDefinition = "bigint")
	private ProductStorage productStorage;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "product_screensize_id", columnDefinition = "bigint")
	private ProductScreenSize productScreenSize;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "product_taxrate_id", columnDefinition = "bigint")
	private ProductTaxRate productTaxRate;
	
	@Column(name = "model_number")
	private String modelNumber;

	@Column(name = "`desc`")
	private String desc;

	public Product(Long id) {
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
