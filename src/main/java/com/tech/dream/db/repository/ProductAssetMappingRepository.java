package com.tech.dream.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tech.dream.db.entity.ProductAssetMapping;

@Repository
public interface ProductAssetMappingRepository extends JpaRepository<ProductAssetMapping, Long> {

    @Modifying
	@Query(value = "UPDATE productassetmapping pam set removed =true, updated_date= now() where pam.product_id = :productId and pam.asset_id = :assetId and removed=FALSE", nativeQuery = true)
	void delete(@Param("productId") Long productId, @Param("assetId") Long assetId);

    @Query(value = "select case when count(1)> 0 then true else false end from productassetmapping pam where pam.product_id = :productId and pam.asset_id = :assetId and removed=FALSE", nativeQuery = true)
	long existsByProductIdAndAssetId(@Param("productId")Long productId, @Param("assetId") Long assetId);
}