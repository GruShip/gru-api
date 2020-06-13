package com.tech.dream.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tech.dream.db.entity.UserBranchMapping;

@Repository
public interface UserBranchMappingRepository extends JpaRepository<UserBranchMapping, Long>{
	
	@Query(value = "select case when count(1)> 0 then true else false end from user u inner join companybranch b on u.company_id = b.company_id where u.id = ?1 and b.id = ?2 and (1 = ?3 or u.company_id = ?3) and u.removed=FALSE and b.removed = FALSE", 
			  nativeQuery = true)
	long isSameClientForUserIdAndBranchIdAndSession(Long userId, Long branchId, Long companyId);
	
	
	@Query(value = "select case when count(1)> 0 then true else false end from userbranchmapping ubm where ubm.user_id = ?1 and ubm.company_branch_id = ?2 and ubm.removed = FALSE", 
			  nativeQuery = true)
	long existsByUserIdAndBranchId(Long userId, Long branchId);
	
	@Query(value = "select case when count(1)> 0 then true else false end from userbranchmapping ubm where ubm.user_id = ?1 and ubm.company_branch_id = ?2 and ubm.id <> ?3 and ubm.removed = FALSE", 
			  nativeQuery = true)
	long existsByUserIdAndBranchIdAndNotId(Long userId, Long branchId, Long id);

	
	@Query(value = "select case when count(1)> 0 then true else false end from userbranchmapping ubm inner join commpanybranch cb on cb.id = userbranchmapping.company_branch_id where ubm.id = ?1 and (1 = ?2 or cb.company_id=?2) and ubm.removed = FALSE",
			nativeQuery = true)
	long existsByUserBranchMappingId(Long id, Long companyId);


	//@Procedure("GetUserAccessBranchList")
	@Query(value = "CALL GetUserAccessBranchList(:user_id);", nativeQuery = true)
	String findUserAccessBranchList(@Param("user_id")Long userId);
	
	@Modifying
	@Query(value = "UPDATE userbranchmapping set removed=1, updated_date= now() where id = :id", nativeQuery = true)
	void delete(@Param("id")Long id);
	
	@Query(value = "select case when count(1)> 0 then true else false end from userbranchmapping ubm where ubm.company_branch_id = :companyBranchId and ubm.removed = FALSE", 
			  nativeQuery = true)
	long existsUsersForCompanyBranch(@Param("companyBranchId") Long id);

	
	@Query(value = "select ubm.id, ubm.user_id, ubm.company_branch_id, u.first_name, u.last_name, u.username, u.email, cb.name as branch_name "
			+ " from userbranchmapping ubm inner join user u on u.id = ubm.user_id "
			+ " inner join companybranch cb on cb.id = ubm.company_branch_id "
			+ " where ubm.removed = FALSE and cb.company_id = (:companyId) order by ubm.id desc", nativeQuery = true)
	List<Object[]> findAllUserBranchMappingData(@Param("companyId")Long companyId);


	@Query(value = "select ubm.id, ubm.user_id, ubm.company_branch_id, u.first_name, u.last_name, u.username, u.email, cb.name as branch_name "
			+ " from userbranchmapping ubm inner join user u on u.id = ubm.user_id "
			+ " inner join companybranch cb on cb.id = ubm.company_branch_id "
			+ " where ubm.removed = FALSE and ubm.id = :id and (1 = :companyId or cb.company_id = (:companyId)) order by ubm.id desc", nativeQuery = true)
	List<Object[]> findUserBranchMappingDataByIdAndCompanyId(@Param("id")Long id, @Param("companyId")Long companyId);

	@Query(value = "select ubm.company_branch_id "
			+ " from userbranchmapping ubm  "
			+ " where ubm.removed = FALSE and ubm.user_id = :userId order by ubm.id desc", nativeQuery = true)
	List<Object[]> findIdListByUserId(@Param("userId")Long userId);


	@Modifying
	@Query(value = "UPDATE userbranchmapping set removed=TRUE, updated_date= now() where user_id = :userId and company_branch_id not in (:companyBranchIdList) and removed=FALSE", nativeQuery = true)
	void removeUserBranchMappingApartFromGivenBranchIdList(@Param("userId")Long userId,@Param("companyBranchIdList") List<Long> companyBranchIdList);

	@Modifying
	@Query(value = "UPDATE userbranchmapping set removed=TRUE, updated_date= now() where user_id = :userId and removed=FALSE", nativeQuery = true)
	void removeAllUserBranchMapping(@Param("userId")Long userId);

}
