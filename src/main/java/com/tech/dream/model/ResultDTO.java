package com.tech.dream.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResultDTO {
    private Object data;
    private ErrorDTO errorDTO;
    private Long totalCount;
    private Long pageNumber;
    private Long pageSize;
}