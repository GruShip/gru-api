package com.tech.dream.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tech.dream.db.entity.ProductScreenSize;

@Repository
public interface ProductScreenSizeRepository extends JpaRepository<ProductScreenSize, Long>{

	@Query(value = "select case when count(1)> 0 then true else false end from productscreensize psc where lower(psc.name) = lower(:name) and psc.removed = FALSE", 
			  nativeQuery = true)
	long existsProductScreenSizeByName(@Param("name") String name);
	
	@Query(value = "select case when count(1)> 0 then true else false end from productscreensize psc where lower(psc.name) = lower(:name) and psc.id <> :id and psc.removed = FALSE", 
			  nativeQuery = true)
	long existsProductScreenSizeByNameAndNotId(@Param("name") String name, @Param("id") Long id);

	@Modifying
	@Query(value = "UPDATE productscreensize psc set name =:name, `desc`=:desc, active=:active, updated_date= now() where psc.id = :id",
          nativeQuery = true)
	void updateProductScreenSize(@Param("id")Long id, @Param("name")String name, @Param("active")Boolean active, @Param("desc")String desc);
	
	@Modifying
	@Query(value = "UPDATE productscreensize set removed=1, updated_date= now() where id = :id", nativeQuery = true)
	void deleteProductScreenSize(@Param("id")Long id);
	
	@Query(value = "select case when count(1)> 0 then true else false end from productscreensize psc where psc.id = :id and psc.removed = FALSE", 
			  nativeQuery = true)
	long existsProductScreenSizeById(@Param("id")Long id);
	
	@Query(value = "select psc.id, psc.name, psc.`desc`, psc.active "
			+ " from productscreensize psc "
			+ " where psc.removed = FALSE order by psc.id desc", nativeQuery = true)
	List<Object[]> findAllProductScreenSizeData();
	
	@Query(value = "select psc.id, psc.name, psc.`desc`, psc.active "
			+ " from productscreensize psc "
			+ " where psc.removed = FALSE and psc.id = :id order by psc.id desc", nativeQuery = true)
	List<Object[]> findProductScreenSizeDataById(@Param("id") Long id);
	
	
	
}
