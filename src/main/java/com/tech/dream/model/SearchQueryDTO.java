package com.tech.dream.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SearchQueryDTO {
    private String searchText;
    private String searchField;
    private String searchType;
}