package com.tech.dream.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "companybranch")
@SequenceGenerator(name="companybranch_seq", sequenceName="companybranch_seq", allocationSize = 1, initialValue = 1)
public class CompanyBranch extends BaseEntity{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", columnDefinition = "bigint not null auto_increment")
	private Long id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "code")
	private String code;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "company_id", columnDefinition = "bigint")
	private Company company;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "parent_company_branch_id", columnDefinition = "bigint")
	private CompanyBranch parentCompanyBranch;
	
	@Column(name = "`desc`")
	private String desc;
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "domain")
	private String domain;
	
	@Column(name = "taxcode")
	private String taxcode;
	
	@Column(name = "phone_number_1")
	private String phoneNumber1;
	
	@Column(name = "phone_number_2")
	private String phoneNumber2;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "primary_address_id", columnDefinition = "bigint")
	private Address primaryAddress;
	
	@Column(name = "is_admin", columnDefinition = "boolean not null default false")
	public Boolean isAdmin;
	
	public CompanyBranch() {}
	public CompanyBranch(Long id) {
		this.id = id;
	}

	public Boolean getIsAdmin() {
		return isAdmin;
	}
	public void setIsAdmin(Boolean isAdmin) {
		this.isAdmin = isAdmin;
	}
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}
	public CompanyBranch getParentCompanyBranch() {
		return parentCompanyBranch;
	}
	public void setParentCompanyBranch(CompanyBranch parentCompanyBranch) {
		this.parentCompanyBranch = parentCompanyBranch;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getTaxcode() {
		return taxcode;
	}
	public void setTaxcode(String taxcode) {
		this.taxcode = taxcode;
	}
	public String getPhoneNumber1() {
		return phoneNumber1;
	}
	public void setPhoneNumber1(String phoneNumber1) {
		this.phoneNumber1 = phoneNumber1;
	}
	public String getPhoneNumber2() {
		return phoneNumber2;
	}
	public void setPhoneNumber2(String phoneNumber2) {
		this.phoneNumber2 = phoneNumber2;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Address getPrimaryAddress() {
		return primaryAddress;
	}
	public void setPrimaryAddress(Address primaryAddress) {
		this.primaryAddress = primaryAddress;
	}
	@PrePersist
    protected void onCreate() {
		super.onCreate();
	}

	@PreUpdate
    protected void onUpdate() {
		super.onUpdate();
	}

}
