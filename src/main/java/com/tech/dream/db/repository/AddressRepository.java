package com.tech.dream.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tech.dream.db.entity.Address;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long>{

	@Modifying
	@Query(value = "UPDATE address a set address_line_1 =:addressLine1, address_line_2 =:addressLine2, city_id = :cityId, state_id = :stateId, country_id=:countryId, pincode=:pincode, address_type=:addressType, updated_date= now() where a.id = :id",
            nativeQuery = true)
	void update(@Param("id") Long id,@Param("addressLine1") String addressLine1,@Param("addressLine2") String addressLine2,@Param("cityId") Long cityId,@Param("stateId") Long stateId, @Param("countryId") Long countryId,
			@Param("pincode") String pincode,@Param("addressType") String addressType);
	
	@Modifying
	@Query(value = "UPDATE address set removed=1, updated_date= now() where id = :id", nativeQuery = true)
	void delete(@Param("id")Long id);
	
	@Query("select case when count(1)> 0 then true else false end from Address a where a.id = (:id) and a.removed = FALSE")
	boolean existsById(@Param("id") Long id);
}
