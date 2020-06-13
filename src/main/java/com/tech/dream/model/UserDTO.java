package com.tech.dream.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class UserDTO {

	private Long id;
	private String firstName;
	private String lastName;
	private String username;
	private String email;
	private String password;
	private String confirmPassword;
	private String type;
	private Long companyId;
	private String phoneNumber1;
	private String phoneNumber2;
	private AddressDTO primaryAddress;
	private Boolean active;
	private Boolean isSystemAdmin;
	private Long userGroupId;
	private List<Long> companyBranchIdList;
	private List<Long> accessCompanyIdList;
	private Boolean isAdmin;
	
}
