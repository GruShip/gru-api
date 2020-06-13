package com.tech.dream.db.repository;

import com.tech.dream.db.entity.OrderBatch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderBatchRepository extends JpaRepository<OrderBatch, Long> {
    
}