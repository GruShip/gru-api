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
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "company")
public class Company extends BaseEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", columnDefinition = "bigint not null auto_increment")
	private Long id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "code")
	private String code;
	
	@Column(name = "company_type")
	private String companyType;
	
	@Column(name = "type")
	private String type;
	
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
	
	
	public Company(Long id) {
		this.id = id;
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
