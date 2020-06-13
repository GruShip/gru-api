package com.tech.dream.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tech.dream.db.entity.ProductType;

@Repository
public interface ProductTypeRepository extends JpaRepository<ProductType, Long>{
	
	@Query(value = "select case when count(1)> 0 then true else false end from producttype pt where lower(pt.name) = lower(:name) and pt.product_brand_id =:productBrandId and pt.removed = FALSE", 
			  nativeQuery = true)
	long existsProductTypeByNameAndBrandId(@Param("name") String name, @Param("productBrandId") Long productBrandId);
	
	@Query(value = "select case when count(1)> 0 then true else false end from producttype pt where lower(pt.name) = lower(:name) and pt.product_brand_id =:productBrandId and pt.id <> :id and pt.removed = FALSE", 
			  nativeQuery = true)
	long existsProductTypeByNameAndBrandIdAndNotId(@Param("name") String name, @Param("productBrandId") Long productBrandId , @Param("id") Long id);

	@Modifying
	@Query(value = "UPDATE producttype pt set name =:name,`desc`=:desc, product_brand_id=:productBrandId, active=:active, updated_date= now() where pt.id = :id",
          nativeQuery = true)
	void updateProductType(@Param("id")Long id, @Param("name")String name, @Param("active")Boolean active, @Param("desc")String desc, @Param("productBrandId")Long productBrandId);
	
	@Modifying
	@Query(value = "UPDATE producttype set removed=1, updated_date= now() where id = :id", nativeQuery = true)
	void deleteProductType(@Param("id")Long id);
	
	@Query(value = "select case when count(1)> 0 then true else false end from producttype pt where pt.id = :id and pt.removed = FALSE", 
			  nativeQuery = true)
	long existsProductTypeById(@Param("id")Long id);
	
	@Query(value = "select pt.id, pt.name, pt.`desc`, pt.active, pt.product_brand_id, pb.name as product_brand_name "
			+ " from producttype pt "
			+ " inner join productbrand pb on pb.id = pt.product_brand_id "
			+ " where pt.removed = FALSE and pt.product_brand_id = :productBrandId order by pt.id desc", nativeQuery = true)
	List<Object[]> findAllProductTypeData(@Param("productBrandId")Long productBrandId);
	
	@Query(value = "select pt.id, pt.name, pt.`desc`, pt.active, pt.product_brand_id, pb.name as product_brand_name "
			+ " from producttype pt "
			+ " inner join productbrand pb on pb.id = pt.product_brand_id "
			+ " where pt.removed = FALSE and pt.id = :id order by pt.id desc", nativeQuery = true)
	List<Object[]> findProductTypeDataById(@Param("id") Long id);
}
