package com.tech.dream.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class AssetDTO {
    private Long id;

	private String assetType;
    private String assetUrl;
    private String uploadType;
    private String fileName;
    private String extension;
    private Boolean active;
    private Long createdBy;
}