package com.tech.dream.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tech.dream.db.entity.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long>{
	
	@Query("select case when count(1)> 0 then true else false end from Company c where lower(c.name) = lower(:name) and c.removed = FALSE")
	boolean existsByName(@Param("name") String name);
	
	@Query("select case when count(1)> 0 then true else false end from Company c where c.id = (:id) and c.removed = FALSE")
	boolean existsById(@Param("id") Long id);
	
	@Query("select case when count(1)> 0 then true else false end from Company c where lower(c.name) = lower(:name) and c.id <> (:id) and c.removed = FALSE")
	boolean existsByNameAndNotId(@Param("name") String name, @Param("id") Long id);

	@Modifying
	@Query(value = "UPDATE company c set code =:code, name =:name, email = :email, `desc` = :desc, domain=:domain, taxcode=:taxcode, phone_number_1=:phoneNumber1, phone_number_2=:phoneNumber2,primary_address_id=:primaryAddressId, active=:active, company_type=:companyType, updated_date= now() where c.id = :id",
            nativeQuery = true)
	void update(@Param("id")Long id, @Param("code")String code, @Param("name")String name, @Param("email")String email, @Param("desc")String desc, @Param("domain")String domain, @Param("taxcode")String taxcode, @Param("phoneNumber1")String phoneNumber1, @Param("phoneNumber2")String phoneNumber2, @Param("active")Boolean active, @Param("primaryAddressId") Long primaryAddressId, @Param("companyType")String companyType);
	
	@Modifying
	@Query(value = "UPDATE company set removed=1, updated_date= now() where id = :id", nativeQuery = true)
	void delete(@Param("id")Long id);

	@Query(value = "select c.id, c.name, c.code, c.`desc`, c.email, c.domain, c.taxcode, c.phone_number_1, c.phone_number_2, a.id as address_id, a.address_line_1, a.address_line_2, a.city_id, a.state_id, a.country_id, a.pincode, a.address_type,c.type "
		+ " from company c left join address a on a.id = c.primary_address_id "
		+ " where c.removed = FALSE order by c.id desc", nativeQuery = true)
	List<Object[]> findAllCompanyData();

	@Query(value = "select c.id, c.name, c.code, c.`desc`, c.email, c.domain, c.taxcode, c.phone_number_1, c.phone_number_2, a.id as address_id, a.address_line_1, a.address_line_2, a.city_id, a.state_id, a.country_id, a.pincode, a.address_type,c.type,c.company_type "
			+ " from company c left join address a on a.id = c.primary_address_id "
			+ " where c.removed = FALSE and c.id = :id", nativeQuery = true)
	List<Object[]> findCompanyDataById(@Param("id") Long id);
	
}
