package com.tech.dream.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SellerProductAssetMappingDTO {
    private Long sellerProductId;
    private Long assetId;
    
    private String assetType;
    private String assetUrl;
    private String uploadType;
    private String fileName;
    private String extension;
    private Boolean active;
}