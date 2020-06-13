package com.tech.dream.model;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PagingSortSearchDTO {
    private Long pageNumber;
    private Long pageSize;
    private List<SearchQueryDTO> search;
    private String sortOrder;
    private String sortField;
}