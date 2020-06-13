package com.tech.dream.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tech.dream.db.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    @Query(value = "select o.id, o.quantity, o.price, o.tax, o.total_price, o.status, o.order_number, cb.name, sp.id , sp.name, p.id, p.model_number "
            + " from `order` o inner join companybranch cb on o.buyer_company_branch_id = cb.id"
            + " inner join sellerproduct sp on sp.id = o.seller_product_id"
            + " inner join product p on p.id = sp.product_id"
			+ " where o.removed = FALSE order by o.id desc", nativeQuery = true)
    List<Object[]> findAllOrderData();
    
    @Query(value = "select o.id, o.quantity, o.price, o.tax, o.total_price, o.status, o.order_number, cb.name, sp.id , sp.name, p.id, p.model_number "
    + " from `order` o inner join companybranch cb on o.buyer_company_branch_id = cb.id"
    + " inner join sellerproduct sp on sp.id = o.seller_product_id"
    + " inner join product p on p.id = sp.product_id"
    + " where o.removed = FALSE and o.id = :id", nativeQuery = true)
    List<Object[]> findOrderDataById(@Param("id") Long id);

    @Query(value = "select case when count(1)> 0 then true else false end from order o "
			+ " where o.buyer_company_branch_id = :companyBranchId and o.removed = FALSE", nativeQuery = true)
	long existsOrderByCompanyBranchId(@Param("companyBranchId")Long companyBranchId);

}
