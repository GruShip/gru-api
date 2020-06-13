package com.tech.dream.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tech.dream.db.entity.ProductCoupon;

@Repository
public interface ProductCouponRepository extends JpaRepository<ProductCoupon, Long>{

	@Query(value = "select case when count(1)> 0 then true else false end from productcoupon pcp where lower(pcp.name) = lower(:name) and pcp.company_id = :companyId and pcp.removed = FALSE", 
			  nativeQuery = true)
	long existsProductCouponByNameAndCompanyId(@Param("name") String name, @Param("companyId") Long companyId);
	
	@Query(value = "select case when count(1)> 0 then true else false end from productcoupon pcp where lower(pcp.name) = lower(:name) and pcp.company_id = :companyId and pcp.id <> :id and pcp.removed = FALSE", 
			  nativeQuery = true)
	long existsProductCouponByNameAndCompanyIdAndNotId(@Param("name") String name, @Param("companyId") Long companyId, @Param("id") Long id);

	@Modifying
	@Query(value = "UPDATE productcoupon pcp set name =:name, `desc`=:desc, active=:active, updated_date= now() where pcp.id = :id",
          nativeQuery = true)
	void updateProductCoupon(@Param("id")Long id, @Param("name")String name, @Param("active")Boolean active, @Param("desc")String desc);
	
	@Modifying
	@Query(value = "UPDATE productcoupon set removed=1, updated_date= now() where id = :id", nativeQuery = true)
	void deleteProductCoupon(@Param("id")Long id);
	
	@Query(value = "select case when count(1)> 0 then true else false end from productcoupon pcp where pcp.id = :id and pcp.removed = FALSE", 
			  nativeQuery = true)
	long existsProductCouponById(@Param("id")Long id);
	
}
