package com.tech.dream.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tech.dream.db.entity.ProductCategory;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long>{

	@Query(value = "select case when count(1)> 0 then true else false end from productcategory pc where lower(pc.name) = lower(:name) and pc.removed = FALSE", 
			  nativeQuery = true)
	long existsProductCategoryByName(@Param("name") String name);
	
	@Query(value = "select case when count(1)> 0 then true else false end from productcategory pc where lower(pc.name) = lower(:name) and pc.id <> :id and pc.removed = FALSE", 
			  nativeQuery = true)
	long existsProductCategoryByNameAndNotId(@Param("name") String name, @Param("id") Long id);
	
	@Modifying
	@Query(value = "UPDATE productcategory pc set name =:name,`desc`=:desc, active=:active, updated_date= now() where pc.id = :id",
            nativeQuery = true)
	void updateProductCategory(@Param("id")Long id, @Param("name")String name, @Param("active")Boolean active, @Param("desc")String desc);
	
	@Modifying
	@Query(value = "UPDATE productcategory set removed=1, updated_date= now() where id = :id", nativeQuery = true)
	void deleteProductCategory(@Param("id")Long id);
	
	@Query(value = "select case when count(1)> 0 then true else false end from productcategory pc where pc.id = :id and pc.removed = FALSE", 
			  nativeQuery = true)
	long existsProductCategoryById(@Param("id")Long id);
	
	@Query(value = "select pc.id, pc.name, pc.`desc`, pc.active "
			+ " from productcategory pc "
			+ " where pc.removed = FALSE order by pc.id desc", nativeQuery = true)
	List<Object[]> findAllProductCategoryData();
	
	@Query(value = "select pc.id, pc.name, pc.`desc`, pc.active "
			+ " from productcategory pc "
			+ " where pc.removed = FALSE and pc.id=:id order by pc.id desc", nativeQuery = true)
	List<Object[]> findProductCategoryDataById(@Param("id") Long id);
	
	// Product Sub Category Queries
	
	@Query(value = "select case when count(1)> 0 then true else false end from productsubcategory psc where lower(psc.name) = lower(:name) and psc.product_category_id =:productCategoryId and psc.removed = FALSE", 
			  nativeQuery = true)
	long existsProductSubCategoryByNameAndCategoryId(@Param("name") String name, @Param("productCategoryId") Long productCategoryId);
	
	@Query(value = "select case when count(1)> 0 then true else false end from productsubcategory psc where lower(psc.name) = lower(:name) and psc.product_category_id =:productCategoryId and psc.id <> :id and psc.removed = FALSE", 
			  nativeQuery = true)
	long existsProductSubCategoryByNameAndCategoryIdAndNotId(@Param("name") String name, @Param("productCategoryId") Long productCategoryId , @Param("id") Long id);

	@Modifying
	@Query(value = "UPDATE productsubcategory psc set name =:name,`desc`=:desc, product_category_id=:productCategoryId, active=:active, updated_date= now() where psc.id = :id",
            nativeQuery = true)
	void updateProductSubCategory(@Param("id")Long id, @Param("name")String name, @Param("active")Boolean active, @Param("desc")String desc, @Param("productCategoryId")Long productCategoryId);
	
	@Modifying
	@Query(value = "UPDATE productsubcategory set removed=1, updated_date= now() where id = :id", nativeQuery = true)
	void deleteProductSubCategory(@Param("id")Long id);
	
	@Query(value = "select case when count(1)> 0 then true else false end from productsubcategory pc where pc.id = :id and pc.removed = FALSE", 
			  nativeQuery = true)
	long existsProductSubCategoryById(@Param("id")Long id);
	
	@Query(value = "select psc.id, psc.name, psc.`desc`, psc.active, psc.product_category_id, pc.name as product_category_name "
			+ " from productsubcategory psc "
			+ " inner join productcategory pc on pc.id = psc.product_category_id "
			+ " where psc.removed = FALSE and psc.product_category_id = :productCategoryId order by psc.id desc", nativeQuery = true)
	List<Object[]> findAllProductSubCategoryData(@Param("productCategoryId")Long productCategoryId);
	
	@Query(value = "select psc.id, psc.name, psc.`desc`, psc.active, psc.product_category_id, pc.name as product_category_name "
			+ " from productsubcategory psc "
			+ " inner join productcategory pc on pc.id = psc.product_category_id "
			+ " where psc.removed = FALSE and psc.id = :id order by psc.id desc", nativeQuery = true)
	List<Object[]> findProductSubCategoryDataById(@Param("id") Long id);
}
