package com.tech.dream.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class AddressDTO {
	private Long id;
	private String addressLine1;
	private String addressLine2;
	private Long cityId;
	private Long stateId;
	public Long countryId;
	private String pincode;
	private String addressType;
	public Boolean removed;
}
