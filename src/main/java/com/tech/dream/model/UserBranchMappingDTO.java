package com.tech.dream.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class UserBranchMappingDTO {

	private Long id;
	private Long userId;
	private Long companyBranchId;
	private String firstName;
	private String lastName;
	private String username;
	private String email;
	private String companyBranchName;
}
