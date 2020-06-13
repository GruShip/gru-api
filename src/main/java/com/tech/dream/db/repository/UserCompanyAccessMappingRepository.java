package com.tech.dream.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tech.dream.db.entity.UserCompanyAccessMapping;

@Repository
public interface UserCompanyAccessMappingRepository extends JpaRepository<UserCompanyAccessMapping, Long>{

	@Modifying
	@Query(value = "UPDATE usercompanyaccessmapping set removed=TRUE, updated_date= now() where user_id = :userId and removed=FALSE", nativeQuery = true)
	void removeAllMappings(@Param("userId") Long userId);

	@Modifying
	@Query(value = "UPDATE usercompanyaccessmapping set removed=TRUE, updated_date= now() where user_id = :userId and company_id not in (:companyIdList) and removed=FALSE", nativeQuery = true)
	void removeMappingApartFromGivenCompanyIdList(@Param("userId") Long userId, @Param("companyIdList") List<Long> companyIdList);

	@Query(value = "select case when count(1)> 0 then true else false end from usercompanyaccessmapping ucam where ucam.user_id = :userId and ucam.company_id = :companyId and ucam.removed = FALSE", 
			  nativeQuery = true)
	long existsMappingByUserIdAndCompanyId(@Param("userId")Long userId, @Param("companyId")Long companyId);

	@Query(value = "select ucam.company_id, ucam.id "
			+ " from usercompanyaccessmapping ucam  "
			+ " where ucam.removed = FALSE and ucam.user_id = :userId order by ucam.id desc", nativeQuery = true)
	List<Object[]> findIdListByUserId(@Param("userId")Long userId);
	
}
