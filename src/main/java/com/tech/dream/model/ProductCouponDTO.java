package com.tech.dream.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ProductCouponDTO {

	private Long id;
	private String name;
	private String desc;
	private String discountType;
	private String type;
	private Long companyId;
	private Double value;
	private Boolean active;
	private Date expiryDate;
	
}
