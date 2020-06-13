package com.tech.dream.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@Entity
@Table(name = "asset")
public class Asset extends BaseEntity{
    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", columnDefinition = "bigint not null auto_increment")
	private Long id;
	
	@Column(name = "asset_type")
    private String assetType;
    
    @Column(name = "asset_url")
    private String assetUrl;
    
    @Column(name = "upload_type")
    private String uploadType;
    
	@Column(name = "file_name")
    private String fileName;
    
    @Column(name = "extension")
	private String extension;

	public Asset(Long id) {
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