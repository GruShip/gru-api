package com.tech.dream;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages={"com.tech.dream"})
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class PinakApplication implements CommandLineRunner{

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		String profile = args.length > 0 ? args[0].toLowerCase().trim(): "staging";
		System.setProperty("env", profile);
		SpringApplication.run(PinakApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
	}

}
