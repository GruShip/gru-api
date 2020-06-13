package com.tech.dream.controller;

import java.io.IOException;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tech.dream.model.AccessModuleDTO;
import com.tech.dream.model.ErrorDTO;
import com.tech.dream.model.ResponseDTO;
import com.tech.dream.model.SessionDTO;
import com.tech.dream.service.accessmodule.AccessModuleService;
import com.tech.dream.util.CommonUtil;
import com.tech.dream.util.Constants.ResponseStatus;

@RestController
@RequestMapping(value="/api/access_module")
public class AccessModuleController {

	@Autowired
	private AccessModuleService accessModuleService;
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public ResponseEntity create(@RequestBody AccessModuleDTO dto, @RequestAttribute(name="session") SessionDTO session) throws IOException, SQLException{
		ResponseDTO response = new ResponseDTO();
		HttpStatus status = HttpStatus.CREATED;
		try {
			Object result = accessModuleService.create(dto, session);
			status = CommonUtil.fillResponseDTO(result, response, status);
		}catch(Exception e) {
			e.printStackTrace();
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			response.setStatus(ResponseStatus.FAILURE);
			response.setError(new ErrorDTO(status, "Error in createAccessModule()", e.getMessage()));
		}
		
		return new ResponseEntity<>(response, status);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/update", method = RequestMethod.PUT)
	public ResponseEntity update(@RequestBody AccessModuleDTO dto, @RequestAttribute(name="session") SessionDTO session) throws IOException, SQLException{
		ResponseDTO response = new ResponseDTO();
		HttpStatus status = HttpStatus.OK;
		try {
			Object result = accessModuleService.update(dto, session);
			status = CommonUtil.fillResponseDTO(result, response, status);
		}catch(Exception e) {
			e.printStackTrace();
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			response.setStatus(ResponseStatus.FAILURE);
			response.setError(new ErrorDTO(status, "Error in updateAccessModule()", e.getMessage()));
		}
		return new ResponseEntity<>(response, status);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	public ResponseEntity delete(@RequestBody AccessModuleDTO dto, @RequestAttribute(name="session") SessionDTO session) throws IOException, SQLException{
		ResponseDTO response = new ResponseDTO();
		HttpStatus status = HttpStatus.OK;
		try {
			Object result = accessModuleService.delete(dto, session);
			status = CommonUtil.fillResponseDTO(result, response, status);
		}catch(Exception e) {
			e.printStackTrace();
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			response.setStatus(ResponseStatus.FAILURE);
			response.setError(new ErrorDTO(status, "Error in deleteAccessModule()", e.getMessage()));
		}
		return new ResponseEntity<>(response, status);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public ResponseEntity list(@RequestAttribute(name="session") SessionDTO session, @RequestParam(name = "company_id", defaultValue = "0") Long companyId) throws IOException, SQLException{
		ResponseDTO response = new ResponseDTO();
		HttpStatus status = HttpStatus.OK;
		try {
			Object result = accessModuleService.list(session, companyId);
			status = CommonUtil.fillResponseDTO(result, response, status);
		}catch(Exception e) {
			e.printStackTrace();
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			response.setStatus(ResponseStatus.FAILURE);
			response.setError(new ErrorDTO(status, "Error in list()", e.getMessage()));
			
		}
		return new ResponseEntity<>(response, status);
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/get", method = RequestMethod.GET)
	public ResponseEntity get(@RequestAttribute(name = "session") SessionDTO session, @RequestParam(name = "id", defaultValue = "0") Long id) throws IOException, SQLException{
		ResponseDTO response = new ResponseDTO();
		HttpStatus status = HttpStatus.OK;
		try {
			Object result = accessModuleService.get(session, id);
			status = CommonUtil.fillResponseDTO(result, response, status);
		}catch(Exception e) {
			e.printStackTrace();
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			response.setStatus(ResponseStatus.FAILURE);
			response.setError(new ErrorDTO(status, "Error in list()", e.getMessage()));
			
		}
		return new ResponseEntity<>(response, status);
	}
	
}

