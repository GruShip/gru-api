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
@Table(name = "productlistingcompanymapping")
public class ProductListingCompanyMapping extends BaseEntity{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", columnDefinition = "bigint not null auto_increment")
	private Long id;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "source_company_id", columnDefinition = "bigint")
	private Company sourceCompany;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "destination_company_id", columnDefinition = "bigint")
	private Company destinationCompany;
	
	@PrePersist
    protected void onCreate() {
		super.onCreate();
	}

	@PreUpdate
    protected void onUpdate() {
		super.onUpdate();
	}
}
