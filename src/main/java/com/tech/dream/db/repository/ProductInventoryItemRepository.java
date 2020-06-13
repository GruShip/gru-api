package com.tech.dream.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tech.dream.db.entity.ProductInventoryItem;

@Repository
public interface ProductInventoryItemRepository extends JpaRepository<ProductInventoryItem, Long>{

	@Modifying
	@Query(value = "UPDATE productinventoryitem set removed=1, updated_date= now() where id = :id", nativeQuery = true)
	void delete(@Param("id")Long id);

	@Query(value = "select case when count(1)> 0 then true else false end from productinventoryitem pii where pii.id = :id and pii.removed = FALSE", nativeQuery = true)
	int existsByProductInventoryItemId(@Param("id")Long id);

	@Query(value = "select case when count(1)> 0 then true else false end from productinventoryitem pii where pii.item_id = :itemId and pii.seller_product_id=:sellerProductId and pii.removed = FALSE", nativeQuery = true)
	long existsByItemIdAndSellerProductId(@Param("itemId")String itemId, @Param("sellerProductId")Long sellerProductId);

	@Query(value = "select case when count(1)> 0 then true else false end from productinventoryitem pii where pii.item_id = :itemId and pii.seller_product_id=:sellerProductId and pii.id <> :id and pii.removed = FALSE ", nativeQuery = true)
	long existsByItemIdAndSellerProductIdAndNotId(@Param("itemId")String itemId, @Param("sellerProductId")Long sellerProductId, @Param("id")Long id);
	
	@Modifying
	@Query(value = "UPDATE productinventoryitem pii set item_id=:itemId, imei_1 = :imei1, imei_2 = :imei2, seller_product_id = :sellerProductId, updated_date= now() where id = :id", nativeQuery = true)
	void update(@Param("id")Long id,@Param("itemId") String itemId, @Param("imei1") String imei1,@Param("imei2") String imei2, @Param("sellerProductId") Long sellerProductId);
	
}
