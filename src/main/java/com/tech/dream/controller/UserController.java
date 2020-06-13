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

import com.tech.dream.model.ErrorDTO;
import com.tech.dream.model.FiltersRequestDTO;
import com.tech.dream.model.ResponseDTO;
import com.tech.dream.model.ResultDTO;
import com.tech.dream.model.SessionDTO;
import com.tech.dream.model.UserDTO;
import com.tech.dream.service.user.UserService;
import com.tech.dream.util.AccessType;
import com.tech.dream.util.CommonUtil;
import com.tech.dream.util.Constants;

@RestController
@RequestMapping(value="/api/user")
public class UserController implements Constants{
	
	@Autowired
	private UserService userService;
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public ResponseEntity create(@RequestBody UserDTO dto, @RequestAttribute(name = "session") SessionDTO session) throws IOException, SQLException{
		ResponseDTO response = new ResponseDTO();
		HttpStatus status = HttpStatus.CREATED;
		try {
			Object result = null;
			if(CommonUtil.isAccessAllowed(session, AccessType.CREATE, AccessModules.USER)) {
				result = userService.createGeneralUser(dto, session);
			}else {
				result = new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have create access for this module.");
			}
			status = CommonUtil.fillResponseDTO(result, response, status);
		}catch(Exception e) {
			e.printStackTrace();
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			response.setStatus(ResponseStatus.FAILURE);
			response.setError(new ErrorDTO(status, "Error in create()", e.getMessage()));
		}
		
		return new ResponseEntity<>(response, status);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/update", method = RequestMethod.PUT)
	public ResponseEntity update(@RequestBody UserDTO dto, @RequestAttribute(name = "session") SessionDTO session) throws IOException, SQLException{
		ResponseDTO response = new ResponseDTO();
		HttpStatus status = HttpStatus.OK;
		try {
			Object result = null;
			if(CommonUtil.isAccessAllowed(session, AccessType.UPDATE, AccessModules.USER)) {
				result = userService.updateGeneralUser(dto, session);
			}else {
				result = new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have update access for this module.");
			}
			status = CommonUtil.fillResponseDTO(result, response, status);
		}catch(Exception e) {
			e.printStackTrace();
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			response.setStatus(ResponseStatus.FAILURE);
			response.setError(new ErrorDTO(status, "Error in update()", e.getMessage()));
		}
		return new ResponseEntity<>(response, status);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	public ResponseEntity delete(@RequestBody UserDTO dto, @RequestAttribute(name = "session") SessionDTO session) throws IOException, SQLException{
		ResponseDTO response = new ResponseDTO();
		HttpStatus status = HttpStatus.OK;
		try {
			Object result = null;
			if(CommonUtil.isAccessAllowed(session, AccessType.DELETE, AccessModules.USER)) {
				result = userService.delete(dto, session);
			}else {
				result = new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have delete access for this module.");
			}
			status = CommonUtil.fillResponseDTO(result, response, status);
		}catch(Exception e) {
			e.printStackTrace();
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			response.setStatus(ResponseStatus.FAILURE);
			response.setError(new ErrorDTO(status, "Error in delete()", e.getMessage()));
			
		}
		return new ResponseEntity<>(response, status);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public ResponseEntity list(@RequestAttribute(name="session") SessionDTO session, @RequestBody FiltersRequestDTO filters) throws IOException, SQLException{
		ResponseDTO response = new ResponseDTO();
		HttpStatus status = HttpStatus.OK;
		try {
			ResultDTO result = new ResultDTO();
			if(CommonUtil.isAccessAllowed(session, AccessType.READ, AccessModules.USER)) {
				result = userService.list(session, filters.getCompanyId(), filters.getFilters());
			}else {
				result.setErrorDTO(new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have read access for this module."));
			}
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
			Object result = null;
			if(CommonUtil.isAccessAllowed(session, AccessType.READ, AccessModules.USER)) {
				result = userService.get(session, id);
			}else {
				result = new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have read access for this module.");
			}
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
