package com.tech.dream.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tech.dream.db.entity.ProductModel;

@Repository
public interface ProductModelRepository extends JpaRepository<ProductModel, Long>{
	
	@Query(value = "select case when count(1)> 0 then true else false end from productmodel pm where lower(pm.name) = lower(:name) and pm.product_type_id =:productTypeId and pm.removed = FALSE", 
			  nativeQuery = true)
	long existsProductModelByNameAndTypeId(@Param("name") String name, @Param("productTypeId") Long productTypeId);
	
	@Query(value = "select case when count(1)> 0 then true else false end from productmodel pm where lower(pm.name) = lower(:name) and pm.product_type_id =:productTypeId and pm.id <> :id and pm.removed = FALSE", 
			  nativeQuery = true)
	long existsProductModelByNameAndTypeIdAndNotId(@Param("name") String name, @Param("productTypeId") Long productTypeId , @Param("id") Long id);

	@Modifying
	@Query(value = "UPDATE productmodel pm set name =:name,`desc`=:desc, product_type_id=:productTypeId, active=:active, updated_date= now() where pm.id = :id",
          nativeQuery = true)
	void updateProductModel(@Param("id")Long id, @Param("name")String name, @Param("active")Boolean active, @Param("desc")String desc, @Param("productTypeId")Long productTypeId);
	
	@Modifying
	@Query(value = "UPDATE productmodel set removed=1, updated_date= now() where id = :id", nativeQuery = true)
	void deleteProductModel(@Param("id")Long id);
	
	@Query(value = "select case when count(1)> 0 then true else false end from productmodel pm where pm.id = :id and pm.removed = FALSE", 
			  nativeQuery = true)
	long existsProductModelById(@Param("id")Long id);
	
	@Query(value = "select pm.id, pm.name, pm.`desc`, pm.active, pm.product_type_id, pt.name as product_type_name, pb.id as product_brand_id, pb.name as product_brand_name "
			+ " from productmodel pm "
			+ " inner join producttype pt on pt.id = pm.product_type_id "
			+ " inner join productbrand pb on pb.id = pt.product_brand_id "
			+ " where pm.removed = FALSE and pm.product_type_id = :productTypeId order by pm.id desc", nativeQuery = true)
	List<Object[]> findAllProductModelData(@Param("productTypeId")Long productTypeId);
	
	@Query(value = "select pm.id, pm.name, pm.`desc`, pm.active, pm.product_type_id, pt.name as product_type_name, pb.id as product_brand_id, pb.name as product_brand_name "
			+ " from productmodel pm "
			+ " inner join producttype pt on pt.id = pm.product_type_id "
			+ " inner join productbrand pb on pb.id = pt.product_brand_id "
			+ " where pm.removed = FALSE and pm.id=:id order by pm.id desc", nativeQuery = true)
	List<Object[]> findProductModelDataById(@Param("id") Long id);
	
}
