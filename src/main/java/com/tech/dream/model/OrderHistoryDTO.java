package com.tech.dream.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderHistoryDTO {
    private Long id;
    private Long orderId;
    private String desc;
    private String status;
    private String createdDate;
}