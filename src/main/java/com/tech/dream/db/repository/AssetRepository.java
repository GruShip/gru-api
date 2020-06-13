package com.tech.dream.db.repository;

import java.util.List;

import com.tech.dream.db.entity.Asset;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long>{
    
    @Modifying
	@Query(value = "UPDATE asset a set removed =true, updated_date= now() where a.id = :id", nativeQuery = true)
	void delete(@Param("id") Long id);

    @Query(value = "select case when count(1)> 0 then true else false end from asset a where a.id = :id and a.removed = FALSE", nativeQuery = true)
    long existsByAssetId(@Param("id")Long id);
    
    @Query(value = "select ass.id, ass.asset_type, ass.asset_url,ass.active, ass.upload_type,ass.file_name, ass.extension from asset ass inner join productassetmapping pasm on ass.id = pasm.asset_id where ass.removed = FALSE and pasm.removed = FALSE and pasm.product_id = :productId ;", nativeQuery = true)
    List<Object[]> findAssetsByProductId(@Param("productId")Long productId);

}