package com.tech.dream.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.tech.dream.model.ErrorDTO;
import com.tech.dream.model.LoginRequestDTO;
import com.tech.dream.model.LoginResponseDTO;
import com.tech.dream.model.ResponseDTO;
import com.tech.dream.model.SessionDTO;
import com.tech.dream.service.security.SecurityService;
import com.tech.dream.service.security.SessionService;
import com.tech.dream.util.CommonUtil;
import com.tech.dream.util.Constants;

@RestController
@RequestMapping(value="/api")
public class SecurityController implements Constants{
	
	@Autowired
	private SecurityService securityService;
	
	@Autowired
	private SessionService sessionService;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/signin", method = RequestMethod.POST)
	public ResponseEntity signin(@RequestBody LoginRequestDTO dto) throws IOException, SQLException{
		ResponseDTO response = new ResponseDTO();
		HttpStatus status = HttpStatus.OK;
		HttpHeaders responseHeaders = new HttpHeaders();
		try {
			Object result = securityService.signin(dto);
			status = CommonUtil.fillResponseDTO(result, response, status);
			if(status == HttpStatus.OK) {
				String tokenHeaderValue = "Bearer "+((LoginResponseDTO)result).getToken();
		    	responseHeaders.set("Authorization", tokenHeaderValue);
			}
		}catch(Exception e) {
			e.printStackTrace();
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			response.setStatus(ResponseStatus.FAILURE);
			response.setError(new ErrorDTO(status, "Error in signin()", e.getMessage()));
		}
		
		return ResponseEntity.status(status).headers(responseHeaders).body(response);
	}

	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/logout", method = RequestMethod.POST)
	public ResponseEntity logout(@RequestAttribute(name = "session") SessionDTO sessionDTO) throws IOException, SQLException{
		ResponseDTO response = new ResponseDTO();
		HttpStatus status = HttpStatus.OK;
		HttpHeaders responseHeaders = new HttpHeaders();
		try {
			Object result = sessionService.logout(sessionDTO);
			status = CommonUtil.fillResponseDTO(result, response, status);
			if(status == HttpStatus.OK) {
				String tokenHeaderValue = "Bearer "+((LoginResponseDTO)result).getToken();
		    	responseHeaders.set("Authorization", tokenHeaderValue);
			}
		}catch(Exception e) {
			e.printStackTrace();
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			response.setStatus(ResponseStatus.FAILURE);
			response.setError(new ErrorDTO(status, "Error in logout()", e.getMessage()));
		}
		
		return ResponseEntity.status(status).headers(responseHeaders).body(response);
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/token/details", method = RequestMethod.GET)
	public ResponseEntity tokenDetails(@RequestAttribute(name = "session") SessionDTO session) throws IOException, SQLException{
		ResponseDTO response = new ResponseDTO();
		HttpStatus status = HttpStatus.OK;
		HttpHeaders responseHeaders = new HttpHeaders();
		try {
			LoginResponseDTO result = new LoginResponseDTO();
			result.setUserId(session.getUserId());
			result.setUserGroupId(session.getUserGroupId());
			result.setCompanyId(session.getCompanyId());
			result.setCompanyType(session.getCompanyType());
			result.setToken(session.getToken());
			result.setAccessModuleList(new ArrayList<>(session.getAccessModuleList().values()));
			status = CommonUtil.fillResponseDTO(result, response, status);
			if(status == HttpStatus.OK) {
				String tokenHeaderValue = "Bearer "+result.getToken();
		    	responseHeaders.set("Authorization", tokenHeaderValue);
			}
		}catch(Exception e) {
			e.printStackTrace();
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			response.setStatus(ResponseStatus.FAILURE);
			response.setError(new ErrorDTO(status, "Error in signin()", e.getMessage()));
		}
		
		return ResponseEntity.status(status).headers(responseHeaders).body(response);
	}
}
