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
@Table(name = "user")
public class User extends BaseEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", columnDefinition = "bigint not null auto_increment")
	private Long id;
	
	@Column(name = "first_name")
	private String firstName;
	
	@Column(name = "last_name")
	private String lastName;
	
	@Column(name = "username")
	private String username;
	
	@Column(name = "password")
	private String password;
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "type")
	private String type;
	
	@Column(name = "is_system_admin", columnDefinition = "boolean not null default false")
	private Boolean isSystemAdmin;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "company_id", columnDefinition = "bigint")
	private Company company;
	
	@Column(name = "phone_number_1")
	private String phoneNumber1;
	
	@Column(name = "phone_number_2")
	private String phoneNumber2;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "primary_address_id", columnDefinition = "bigint")
	private Address primaryAddress;
	
	@Column(name = "is_admin", columnDefinition = "boolean not null default false")
	public Boolean isAdmin;
	
	public User(Long id) {
		this.id = id;
	}
	
	@PrePersist
    protected void onCreate() {
		super.onCreate();
		this.isSystemAdmin = this.isSystemAdmin == null ? false : this.isSystemAdmin;
	}

	@PreUpdate
    protected void onUpdate() {
		super.onUpdate();
		this.isSystemAdmin = this.isSystemAdmin == null ? false : this.isSystemAdmin;
	}
	
}
