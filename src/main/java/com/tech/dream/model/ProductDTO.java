package com.tech.dream.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ProductDTO extends AbstractProductConfigurationDTO {

	private Long id;
	private Boolean active;
	private String modelNumber;
	private String desc;
	private List<AssetDTO> productAssets;

}
