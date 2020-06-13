package com.tech.dream.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tech.dream.db.entity.ProductColor;

@Repository
public interface ProductColorRepository extends JpaRepository<ProductColor, Long>{

	@Query(value = "select case when count(1)> 0 then true else false end from productcolor pco where lower(pco.name) = lower(:name) and pco.removed = FALSE", 
			  nativeQuery = true)
	long existsProductColorByName(@Param("name") String name);
	
	@Query(value = "select case when count(1)> 0 then true else false end from productcolor pco where lower(pco.name) = lower(:name) and pco.id <> :id and pco.removed = FALSE", 
			  nativeQuery = true)
	long existsProductColorByNameAndNotId(@Param("name") String name, @Param("id") Long id);

	@Modifying
	@Query(value = "UPDATE productcolor pco set name =:name, `desc`=:desc, active=:active, updated_date= now() where pco.id = :id",
          nativeQuery = true)
	void updateProductColor(@Param("id")Long id, @Param("name")String name, @Param("active")Boolean active, @Param("desc")String desc);
	
	@Modifying
	@Query(value = "UPDATE productcolor set removed=1, updated_date= now() where id = :id", nativeQuery = true)
	void deleteProductColor(@Param("id")Long id);
	
	@Query(value = "select case when count(1)> 0 then true else false end from productcolor pco where pco.id = :id and pco.removed = FALSE", 
			  nativeQuery = true)
	long existsProductColorById(@Param("id")Long id);
	
	@Query(value = "select pco.id, pco.name, pco.`desc`, pco.active "
			+ " from productcolor pco "
			+ " where pco.removed = FALSE order by pco.id desc", nativeQuery = true)
	List<Object[]> findAllProductColorData();
	
	@Query(value = "select pco.id, pco.name, pco.`desc`, pco.active "
			+ " from productcolor pco "
			+ " where pco.removed = FALSE and pco.id = :id order by pco.id desc", nativeQuery = true)
	List<Object[]> findProductColorDataById(@Param("id") Long id);
	
	
	
}
