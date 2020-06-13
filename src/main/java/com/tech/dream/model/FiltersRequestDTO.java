package com.tech.dream.model;

import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
public class FiltersRequestDTO {
    private Long companyId;
    private Long productCategoryId;
    private Long productTypeId; 
    private Long productBrandId;
    private Long sellerProductId;
    private String orderType;
    private PagingSortSearchDTO filters;
}