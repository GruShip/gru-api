package com.tech.dream.db.repository;

import java.util.List;

import com.tech.dream.db.entity.ProductTaxRate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductTaxRateRepository extends JpaRepository<ProductTaxRate, Long>{

	@Query(value = "select case when count(1)> 0 then true else false end from producttaxrate ptr where tax_percentage = :taxPercentage and ptr.removed = FALSE", 
			  nativeQuery = true)
	long existsProductTaxRateByTaxPercentage(@Param("taxPercentage") Double taxPercentage);
	
	@Query(value = "select case when count(1)> 0 then true else false end from producttaxrate ptr where tax_percentage = :taxPercentage and ptr.id <> :id and ptr.removed = FALSE", 
			  nativeQuery = true)
	long existsProductTaxRateByTaxPercentageAndNotId(@Param("taxPercentage") Double taxPercentage, @Param("id") Long id);

	@Modifying
	@Query(value = "UPDATE producttaxrate ptr set tax_percentage = :taxPercentage, `desc`=:desc, active=:active, updated_date= now() where ptr.id = :id",
          nativeQuery = true)
	void updateProductTaxRate(@Param("id")Long id, @Param("taxPercentage")Double taxPercentage, @Param("active")Boolean active, @Param("desc")String desc);
	
	@Modifying
	@Query(value = "UPDATE producttaxrate set removed=1, updated_date= now() where id = :id", nativeQuery = true)
	void deleteProductTaxRate(@Param("id")Long id);
	
	@Query(value = "select case when count(1)> 0 then true else false end from producttaxrate ptr where ptr.id = :id and ptr.removed = FALSE", 
			  nativeQuery = true)
	long existsProductTaxRateById(@Param("id")Long id);
	
	@Query(value = "select ptr.id, ptr.tax_percentage, ptr.`desc`, ptr.active "
			+ " from producttaxrate ptr "
			+ " where ptr.removed = FALSE order by ptr.id desc", nativeQuery = true)
	List<Object[]> findAllProductTaxRateData();
	
	@Query(value = "select ptr.id, ptr.tax_percentage, ptr.`desc`, ptr.active "
			+ " from producttaxrate ptr "
			+ " where ptr.removed = FALSE and ptr.id = :id order by ptr.id desc", nativeQuery = true)
	List<Object[]> findProductTaxRateDataById(@Param("id") Long id);
	
}
