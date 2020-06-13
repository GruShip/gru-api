package com.tech.dream.config;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.tech.dream.aspect.Log;
import com.tech.dream.auth.filter.AuthenticationFilter;
import com.tech.dream.auth.filter.CorsFilter;


@Configuration  
public class MVCConfig implements WebMvcConfigurer{
	
	
	@Autowired
	private AuthenticationFilter authFilter;
	
	@Autowired
	private CorsFilter corsFilter;
	
	private static @Log Logger logger;
    
//    @Bean
//	 public MappingJackson2HttpMessageConverter jackson2HttpMessageConverter() {
//		MappingJackson2HttpMessageConverter messageConvertor = new MappingJackson2HttpMessageConverter();
//		return messageConvertor;
//	}
//	
//	@Override
//	 public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
//	  converters.add(jackson2HttpMessageConverter());
//	  //super.addDefaultHttpMessageConverters();
//	 }
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean
    public FilterRegistrationBean authFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(authFilter);
        registration.addUrlPatterns("/api/company/*", "/api/company_branch/*", "/api/user/*", 
        							"/api/user_group/*", "/api/user_branch_mapping/*", "/api/access_module/*", 
        							"/api/user_group_mapping/*", "/api/user_group_access_mapping/*", "/api/logout",
        							"/api/product/*", "/api/sellerproduct/*", "/api/marketplace/*", "/api/order/*", "/api/token/details");
        registration.setOrder(2);
        return registration;
    }
	
	@Bean
    public FilterRegistrationBean corsFilterBean() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(corsFilter);
        registration.addUrlPatterns("*");
        registration.setOrder(1);
        return registration;
    }
	
	@Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods("*")
                .allowedOrigins("*")
                .allowedHeaders("*");
    }
}