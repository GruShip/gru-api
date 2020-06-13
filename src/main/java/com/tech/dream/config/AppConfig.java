package com.tech.dream.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tech.dream.util.Constants;

@Configuration
@PropertySources({
	@PropertySource({"classpath:application.properties"}),
	@PropertySource({"classpath:digitalocean.properties"}),
	@PropertySource({"classpath:hikaricp.properties"}),
	@PropertySource({"classpath:${env}/hikaricp.properties"}),
	@PropertySource({"classpath:${env}/digitalocean.properties"}),
	@PropertySource(value = {"file:${external.config.path}/hikaricp.properties"},ignoreResourceNotFound=true)
})
public class AppConfig {
	
	@Bean
	public Gson gson(){
		return new GsonBuilder().setDateFormat(Constants.TZ_DATE_FORMAT).create();
	}

}
