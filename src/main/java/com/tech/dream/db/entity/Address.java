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

import org.springframework.util.StringUtils;

@Entity
@Table(name = "address")
public class Address extends BaseEntity{
	
	public Address() {}
	public Address(Long id) {this.id =id;}
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", columnDefinition = "bigint not null auto_increment")
	private Long id;

	@Column(name = "address_line_1")
	private String addressLine1;
	
	@Column(name = "address_line_2")
	private String addressLine2;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "city_id", columnDefinition = "bigint")
	private City city;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "state_id", columnDefinition = "bigint")
	private State state;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "country_id", columnDefinition = "bigint")
	public Country country;
	
	@Column(name = "pincode")
	private String pincode;
	
	@Column(name = "address_type")
	private String addressType;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	public String getAddressLine2() {
		return addressLine2;
	}

	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	public City getCity() {
		return city;
	}
	public void setCity(City city) {
		this.city = city;
	}
	public State getState() {
		return state;
	}
	public void setState(State state) {
		this.state = state;
	}
	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public String getPincode() {
		return pincode;
	}

	public void setPincode(String pincode) {
		this.pincode = pincode;
	}

	public String getAddressType() {
		return addressType;
	}

	public void setAddressType(String addressType) {
		this.addressType = addressType;
	}

	@PrePersist
    protected void onCreate() {
		super.onCreate();
		if(StringUtils.isEmpty(addressType)) {
			this.addressType = "PRIMARY";
		}
	}

	@PreUpdate
    protected void onUpdate() {
		super.onUpdate();
		if(StringUtils.isEmpty(addressType)) {
			this.addressType = "PRIMARY";
		}
	}
	
}
