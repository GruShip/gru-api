package com.tech.dream.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "productcoupon")
public class ProductCoupon extends BaseEntity{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", columnDefinition = "bigint not null auto_increment")
	private Long id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "`desc`")
	private String desc;
	
	@Column(name = "discount_type")
	private String discountType;
	
	@Column(name = "value")
	private Double value;
	
	@Column(name = "type")
	private String type;
	
	@Column(name = "expiry_date")
	private Date expiryDate;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "company_id", columnDefinition = "bigint")
	private Company company;
	
	public ProductCoupon(Long id) {
		this.id = id;
	}

}
