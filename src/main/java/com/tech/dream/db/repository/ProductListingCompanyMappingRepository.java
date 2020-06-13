package com.tech.dream.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tech.dream.db.entity.ProductListingCompanyMapping;

@Repository
public interface ProductListingCompanyMappingRepository extends JpaRepository<ProductListingCompanyMapping, Long>{
	
	@Modifying
	@Query(value = "UPDATE productlistingcompanymapping set removed=TRUE, updated_date= now() where source_company_id = :sourceCompanyId and ((destination_company_id is NULL and 0 not in (:destinationCompanyIdList)) or (destination_company_id not in (:destinationCompanyIdList))) and removed=FALSE", nativeQuery = true)
	void removeProductListingCompanyMappingApartFromGivenDestCompanyIdList(@Param("sourceCompanyId") Long sourceCompanyId, @Param("destinationCompanyIdList") List<Long> destinationCompanyIdList);

	@Query(value = "select plcm.id "
			+ " from productlistingcompanymapping plcm "
			+ " where plcm.removed = FALSE and plcm.source_company_id = :sourceCompanyId and ((0 = :destinationCompanyId and destination_company_id is NULL) or destination_company_id = :destinationCompanyId) order by plcm.id desc", nativeQuery = true)
	List<Object[]> getIdBySourceCompanyIdAndDestinationCompanyIdId(@Param("sourceCompanyId")Long sourceCompanyId, @Param("destinationCompanyId")Long destinationCompanyId);

	@Modifying
	@Query(value = "UPDATE productlistingcompanymapping set removed=TRUE, updated_date= now() where source_company_id = :sourceCompanyId and removed=FALSE", nativeQuery = true)
	void removeAllProductListingCompanyMapping(@Param("sourceCompanyId")Long sourceCompanyId);

	@Query(value = "select plcm.destination_company_id "
			+ " from productlistingcompanymapping plcm "
			+ " where plcm.removed = FALSE and plcm.source_company_id = :sourceCompanyId order by plcm.id desc", nativeQuery = true)
	List<Object[]> getProductListingCompanyIdList(@Param("sourceCompanyId")Long sourceCompanyId);
}
