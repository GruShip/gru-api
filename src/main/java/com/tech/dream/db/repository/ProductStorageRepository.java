package com.tech.dream.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tech.dream.db.entity.ProductStorage;

@Repository
public interface ProductStorageRepository extends JpaRepository<ProductStorage, Long>{

	@Query(value = "select case when count(1)> 0 then true else false end from productstorage pst where lower(pst.name) = lower(:name) and pst.removed = FALSE", 
			  nativeQuery = true)
	long existsProductStorageByName(@Param("name") String name);
	
	@Query(value = "select case when count(1)> 0 then true else false end from productstorage pst where lower(pst.name) = lower(:name) and pst.id <> :id and pst.removed = FALSE", 
			  nativeQuery = true)
	long existsProductStorageByNameAndNotId(@Param("name") String name, @Param("id") Long id);

	@Modifying
	@Query(value = "UPDATE productstorage pst set name =:name, `desc`=:desc, active=:active, updated_date= now() where pst.id = :id",
          nativeQuery = true)
	void updateProductStorage(@Param("id")Long id, @Param("name")String name, @Param("active")Boolean active, @Param("desc")String desc);
	
	@Modifying
	@Query(value = "UPDATE productstorage set removed=1, updated_date= now() where id = :id", nativeQuery = true)
	void deleteProductStorage(@Param("id")Long id);
	
	@Query(value = "select case when count(1)> 0 then true else false end from productstorage pst where pst.id = :id and pst.removed = FALSE", 
			  nativeQuery = true)
	long existsProductStorageById(@Param("id")Long id);
	
	@Query(value = "select pst.id, pst.name, pst.`desc`, pst.active "
			+ " from productstorage pst "
			+ " where pst.removed = FALSE order by pst.id desc", nativeQuery = true)
	List<Object[]> findAllProductStorageData();
	
	@Query(value = "select pst.id, pst.name, pst.`desc`, pst.active "
			+ " from productstorage pst "
			+ " where pst.removed = FALSE and pst.id=:id order by pst.id desc", nativeQuery = true)
	List<Object[]> findProductStorageDataById(@Param("id") Long id);
	
	
	
}
