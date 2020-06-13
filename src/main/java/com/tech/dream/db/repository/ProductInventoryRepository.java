package com.tech.dream.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tech.dream.db.entity.ProductInventory;

@Repository
public interface ProductInventoryRepository extends JpaRepository<ProductInventory, Long>{

	@Modifying
	@Query(value = "UPDATE productinventory set removed=1, updated_date= now() where id = :id", nativeQuery = true)
	void delete(@Param("id")Long id);

	@Query(value = "select case when count(1)> 0 then true else false end from productinventory pi where pi.id = :id and pi.removed = FALSE", nativeQuery = true)
	int existsByProductInventoryId(@Param("id")Long id);

	@Query(value = "select pi.quantity, pi.operation_type, pi.seller_product_id from productinventory pi where pi.id = :id ", nativeQuery = true)
	List<Object[]> findInventoryInfo(@Param("id")Long id);
	
}
