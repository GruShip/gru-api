package com.tech.dream.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class CompanyDTO {
	
	private Long id;
	private String name;
	private String code;
	private String desc;
	private String email;
	private String domain;
	private String taxcode;
	private String phoneNumber1;
	private String phoneNumber2;
	private AddressDTO primaryAddress;
	public Boolean active;
	public String type;	
	private String companyType;
	public List<Long> productListingCompanyIdList; 
}
