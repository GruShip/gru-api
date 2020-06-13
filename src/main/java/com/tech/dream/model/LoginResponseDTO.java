package com.tech.dream.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class LoginResponseDTO {
	
	private Long userId;
	private Long userGroupId;
	private Long companyId;
	private String firstName;
	private String lastName;
	private String email;
	private String phoneNumber1;
	private Long fieldAgentId;
	private String type;
	private String token;
	private String phoneNumber2;
	private String companyType;
	private List<UserGroupAccessMappingDTO> accessModuleList;
}
