package com.tech.dream.auth.filter;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import com.google.gson.Gson;
import com.tech.dream.aspect.Log;
import com.tech.dream.model.ErrorDTO;
import com.tech.dream.model.ResponseDTO;
import com.tech.dream.model.SessionDTO;
import com.tech.dream.service.security.SessionService;
import com.tech.dream.util.Constants;


@Component
public class AuthenticationFilter extends GenericFilterBean implements Constants{
	
	private static @Log Logger logger;
	
	@Autowired
	private SessionService sessionService;
	
	@Autowired
	private Gson gson;
	
	@SuppressWarnings({ "unchecked", "rawtypes", "resource" })
	@Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		long start = System.currentTimeMillis();
		HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String authorization = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if(StringUtils.isEmpty(authorization)) {
        	ErrorDTO error = new ErrorDTO(HttpStatus.UNAUTHORIZED, "Token is invalid or expired.");
        	ResponseDTO responseDTO = new ResponseDTO(ResponseStatus.FAILURE, null, error, 0L, 0L, 0L);
        	httpResponse.setStatus(error.getStatus().value());
    		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    		try {
    			PrintWriter out=null;
    			out = response.getWriter();
    			String data= gson.toJson(responseDTO);
    			out.println(data);
    		}catch (IOException e) {
    			logger.error("Error while writing response in preHandle() RequestInterceptor: ",e);
    		}
        	return;
        }
        String authorizationPrefix = "Bearer ";
        String token = authorization.substring(authorizationPrefix.length());
        
        
        
        logger.debug("AuthenticationFilter time taken: "+(System.currentTimeMillis() - start));

       Object result = sessionService.getSession(token);
        if(result instanceof ErrorDTO) {
        	ErrorDTO error = (ErrorDTO) result;
        	ResponseDTO responseDTO = new ResponseDTO(ResponseStatus.FAILURE, null, error, 0L, 0L, 0L);
        	httpResponse.setStatus(error.getStatus().value());
    		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    		try {
    			PrintWriter out=null;
    			out = response.getWriter();
    			String data= gson.toJson(responseDTO);
    			out.println(data);
    		}catch (IOException e) {
    			logger.error("Error while writing response in preHandle() RequestInterceptor: ",e);
    		}
        	return;
        }
        
        httpRequest.setAttribute("session", (SessionDTO)result);
        chain.doFilter(request, response);
    }

	private Boolean validate(){
		
		return true;
	}
	
}
