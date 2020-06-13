package com.tech.dream.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductAssetMappingDTO {
    private Long productId;
    private Long assetId;
    
    private String assetType;
    private String assetUrl;
    private String uploadType;
    private String fileName;
    private String extension;
    private Boolean active;
}