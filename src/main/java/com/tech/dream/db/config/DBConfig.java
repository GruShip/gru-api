package com.tech.dream.db.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableJpaRepositories(basePackages = "com.tech.dream.db.repository")
public class DBConfig {
	
	@Value("${spring.datasource.hikari.maximum-pool-size}")
	private Integer maxPoolSize;
	
	@Value("${spring.datasource.hikari.minimum-idle}")
	private Integer minIdle;
	
	@Value("${spring.datasource.url}")
	private String dataSourceUrl;
	
	@Value("${spring.datasource.username}")
	private String username;
	
	@Value("${spring.datasource.password}")
	private String password;
	
	@Value("${spring.jpa.properties.hibernate.dialect}")
	private String hibernateDialect;
	
	@Value("${spring.jpa.hibernate.ddl-auto}")
	private String hibernateHBM2DDLAuto;
	
	@Value("${spring.jpa.hibernate.naming-strategy}")
	private String namingStrategy;
	
	@Value("${spring.jpa.show-sql}")
	private String showSQL;
	
	@Value("${spring.jpa.format-sql}")
	private String formatSQL;
	
	private String driverClassName = "com.mysql.jdbc.Driver";

	public DataSource getDataSource(){
		HikariConfig config = new HikariConfig(); 
		//config.setDriverClassName(driverClassName);
		config.setJdbcUrl(dataSourceUrl);
		config.setUsername(username);
		config.setPassword(password);
		config.setMaximumPoolSize(maxPoolSize);
		config.setMinimumIdle(minIdle);
		config.setConnectionTestQuery("SELECT 1");
		config.setIdleTimeout(0);
		config.setLeakDetectionThreshold(900000);
		HikariDataSource ds = new HikariDataSource(config);
		return ds;
	}
	
	@Bean(name="entityManagerFactory")
	@Qualifier("entityManagerFactory")
	public LocalContainerEntityManagerFactoryBean getEntityManagerFactory(){
		LocalContainerEntityManagerFactoryBean entityManagerFactory  = new LocalContainerEntityManagerFactoryBean();
		entityManagerFactory.setDataSource(getDataSource());
		entityManagerFactory.setJpaVendorAdapter(getJpaVendorAdapter());
		entityManagerFactory.setJpaProperties(getJpaProperties());
		entityManagerFactory.setPackagesToScan("com.tech.dream.db.entity");
		entityManagerFactory.setPersistenceUnitName("dream_unit");
		return entityManagerFactory;
	}
	
	private Properties getJpaProperties() {
		Properties properties = new Properties();
		properties.setProperty("hibernate.dialect", hibernateDialect);
		properties.setProperty("hibernate.hbm2ddl.auto", hibernateHBM2DDLAuto);
		properties.setProperty("hibernate.ejb.naming_strategy", namingStrategy);
		properties.setProperty("hibernate.show_sql", "true");
		properties.setProperty("hibernate.format_sql", "true");
		properties.setProperty("hibernate.archive.autodetection", "false");
		properties.setProperty("hibernate.proc.param_null_passing", "true");
		properties.setProperty("hibernate.temp.use_jdbc_metadata_defaults", "false");
		/*properties.setProperty("hibernate.id.new_generator_mappings", "true");
		properties.setProperty("hibernate.order_inserts", "true");
		properties.setProperty("hibernate.order_updates", "true");
		properties.setProperty("hibernate.jdbc.batch_size", "10000");
		properties.setProperty("hibernate.jdbc.batch_versioned_data", "true");*/
		return properties;
	}
	
	@Bean(name = "transactionManager")
    public PlatformTransactionManager accountTransactionManager(
            @Qualifier("entityManagerFactory") EntityManagerFactory accountEntityManagerFactory) {
        return new JpaTransactionManager(accountEntityManagerFactory);
    }
	
	public Connection connection(){
		Connection dbConnection = null;
		try {
			Class.forName(driverClassName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			//logger.error(e.getMessage());
		}
		try {
			dbConnection = DriverManager.getConnection(dataSourceUrl, username, password);
			return dbConnection;
		} catch (SQLException e) {
			e.printStackTrace();
			//logger.error("Error while creating db connection {}",e);
		}
		return dbConnection;
	}
	
	public void close(Connection dbConnection){
		if (dbConnection != null) {
			try {
				dbConnection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public JpaVendorAdapter getJpaVendorAdapter(){
		HibernateJpaVendorAdapter jpaAdaptor = new HibernateJpaVendorAdapter();
		return jpaAdaptor;
	}
	
}
