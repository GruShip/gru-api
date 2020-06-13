package com.tech.dream.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class CompanyBranchDTO {
	private Long id;
	private String name;
	private String code;
	private Long companyId;
	private Long parentBranchId;
	private String desc;
	private String email;
	private String domain;
	private String taxcode;
	private String phoneNumber1;
	private String phoneNumber2;
	private AddressDTO primaryAddress;
	private Boolean active;
	private String parentBranchName;
	private Boolean isAdmin;
}
