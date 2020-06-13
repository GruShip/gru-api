package com.tech.dream.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tech.dream.db.entity.Country;

@Repository
public interface GeoRepository extends JpaRepository<Country, Long>{
	
	@Query(value = "select c.id, c.name, c.short_code, c.phone_code "
			+ " from country c order by c.name asc",
			nativeQuery = true)
	List<Object[]> findAllCountry();

	@Query(value = "select s.id, s.name, s.country_id "
			+ " from state s where (0 = :countryId or s.country_id = :countryId) order by s.name asc",
			nativeQuery = true)
	List<Object[]> findAllState(@Param("countryId")Long countryId);
	
	@Query(value = "select c.id, c.name, c.state_id "
			+ " from city c where (0 = :stateId or c.state_id = :stateId) order by c.name asc",
			nativeQuery = true)
	List<Object[]> findAllCity(@Param("stateId")Long stateId);
}
