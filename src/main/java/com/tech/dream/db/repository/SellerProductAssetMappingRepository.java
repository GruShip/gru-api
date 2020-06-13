package com.tech.dream.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tech.dream.db.entity.SellerProductAssetMapping;

@Repository
public interface SellerProductAssetMappingRepository extends JpaRepository<SellerProductAssetMapping, Long> {
    
    @Modifying
	@Query(value = "UPDATE sellerproductassetmapping spam set removed =true, updated_date= now() where spam.seller_product_id = :sellerProductId and spam.asset_id = :assetId and spam.removed=FALSE", nativeQuery = true)
	void delete(@Param("sellerProductId") Long sellerProductId, @Param("assetId") Long assetId);

    @Query(value = "select case when count(1)> 0 then true else false end from sellerproductassetmapping spam where spam.seller_product_id = :sellerProductId and spam.asset_id = :assetId and spam.removed=FALSE", nativeQuery = true)
	long existsBySellerProductIdAndAssetId(@Param("sellerProductId")Long sellerProductId, @Param("assetId") Long assetId);
    
}