package com.tech.dream.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tech.dream.db.entity.CompanyBranch;

@Repository
public interface CompanyBranchRepository extends JpaRepository<CompanyBranch, Long>{
	
	@Query(value = "select case when count(1)> 0 then true else false end from companybranch b where b.company_id = ?2 and lower(b.name) = lower(?1) and b.removed = FALSE", 
			  nativeQuery = true)
	long existsByNameAndCompanyId(String name, Long companyId);
	
	@Query(value = "select case when count(1)> 0 then true else false end from companybranch b where b.company_id = ?2 and lower(b.name) = lower(?1) and b.id <> ?3 and b.removed = FALSE", 
			  nativeQuery = true)
	long existsByNameAndCompanyIdAndNotId(String name, Long companyId, Long id);
	
	//@Query("select case when count(b)> 0 then true else false end from CompanyBranch b where b.id = (:id) and b.removed = FALSE")
	@Query(value = "select case when count(1)> 0 then true else false end from companybranch b where b.id = :id and (1 = :companyId or b.company_id = (:companyId)) and b.removed = FALSE",
			nativeQuery = true)
	long existsByCompanyBranchIdAndCompanyId(@Param("id")Long id, @Param("companyId") Long companyId);
	
	@Query(value = "select case when count(1)> 0 then true else false end from companybranch b where b.id = :id and b.is_admin=true and b.removed = FALSE",
			nativeQuery = true)
	long isAdminCompanyBranch(@Param("id")Long id);
	
	@Query(value = "select case when count(1)> 0 then true else false end from companybranch b where b.id = :id and b.removed=FALSE and b.company_id = (select company_id from companybranch pb where pb.id = :parentBranchId and pb.removed=FALSE)",
			nativeQuery = true)
	long isSameCompanyForCompanyBranchIdAndParentCompanyBranchId(@Param("id")Long id, @Param("parentBranchId") Long parentBranchId);

	@Modifying
	@Query(value = "UPDATE companybranch cb set code =:code, name =:name,`desc`=:desc, parent_company_branch_id = :parentCompanyBranchId,primary_address_id=:primaryAddressId, active=:active, updated_date= now() where cb.id = :id",
            nativeQuery = true)
	void update(@Param("id")Long id, @Param("code")String code, @Param("name")String name, @Param("parentCompanyBranchId")Long parentCompanyBranchId, @Param("active")Boolean active, @Param("primaryAddressId") Long primaryAddressId, @Param("desc")String desc);
	
	@Modifying
	@Query(value = "UPDATE companybranch set removed=1, updated_date= now() where id = :id", nativeQuery = true)
	void delete(@Param("id")Long id);

	@Query(value = "select cb.id, cb.name, cb.code, cb.`desc`, cb.email, cb.domain, cb.taxcode, cb.phone_number_1, cb.phone_number_2, a.id as address_id, a.address_line_1, a.address_line_2, a.city_id, a.state_id, a.country_id, a.pincode, a.address_type, pcb.id as pcb_id, pcb.name as pcb_name "
			+ " from companybranch cb left join address a on a.id = cb.primary_address_id left join companybranch pcb on pcb.id = cb.parent_company_branch_id"
			+ " where cb.removed = FALSE and cb.company_id = :companyId order by cb.id desc", nativeQuery = true)
	List<Object[]> findAllCompanyBranchData(@Param("companyId")Long companyId);

	
	@Query(value = "select cb.id, cb.name, cb.code, cb.`desc`, cb.email, cb.domain, cb.taxcode, cb.phone_number_1, cb.phone_number_2, a.id as address_id, a.address_line_1, a.address_line_2, a.city_id, a.state_id, a.country_id, a.pincode, a.address_type, pcb.id as pcb_id, pcb.name as pcb_name, cb.is_admin "
			+ " from companybranch cb left join address a on a.id = cb.primary_address_id left join companybranch pcb on pcb.id = cb.parent_company_branch_id"
			+ " where cb.removed = FALSE and cb.id = :id", nativeQuery = true)
	List<Object[]> findCompanyDataById(@Param("id")Long id);

	@Query(value = "select case when count(1)> 0 then true else false end from companybranch b where b.company_id = :companyId and b.id = :id and b.removed = FALSE", 
			  nativeQuery = true)
	long existsByIdAndCompanyId(@Param("companyId")Long companyId, @Param("id")Long id);

	@Query(value = "select case when count(1)> 0 then true else false end from companybranch b where b.company_id = :companyId and b.removed = FALSE", 
			  nativeQuery = true)
	int existsBranchForCompany(@Param("companyId")Long companyId);

}
