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
import com.tech.dream.model.ProductMemoryDTO;
import com.tech.dream.model.ResponseDTO;
import com.tech.dream.model.ResultDTO;
import com.tech.dream.model.SessionDTO;
import com.tech.dream.service.product.ProductMemoryService;
import com.tech.dream.util.AccessType;
import com.tech.dream.util.CommonUtil;
import com.tech.dream.util.Constants.AccessModules;
import com.tech.dream.util.Constants.DisplayModuleNames;
import com.tech.dream.util.Constants.ResponseStatus;

@RestController
@RequestMapping(value="/api/product/")
public class ProductMemoryController {
	
	@Autowired
	private ProductMemoryService productMemoryService;
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/memory/create", method = RequestMethod.POST)
	public ResponseEntity create(@RequestBody ProductMemoryDTO dto, @RequestAttribute(name="session") SessionDTO session) throws IOException, SQLException{
		ResponseDTO response = new ResponseDTO();
		HttpStatus status = HttpStatus.CREATED;
		try {
			Object result = null;
			if(CommonUtil.isAccessAllowed(session, AccessType.CREATE, AccessModules.PRODUCTCONFIGURATION_ADMIN)) {
				result = productMemoryService.create(dto, session);
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
	@RequestMapping(value = "/memory/update", method = RequestMethod.PUT)
	public ResponseEntity update(@RequestBody ProductMemoryDTO dto, @RequestAttribute(name="session") SessionDTO session) throws IOException, SQLException{
		ResponseDTO response = new ResponseDTO();
		HttpStatus status = HttpStatus.OK;
		try {
			Object result = null;
			if(CommonUtil.isAccessAllowed(session, AccessType.UPDATE, AccessModules.PRODUCTCONFIGURATION_ADMIN)) {
				result = productMemoryService.update(dto, session);
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
	@RequestMapping(value = "/memory/delete", method = RequestMethod.DELETE)
	public ResponseEntity delete(@RequestBody ProductMemoryDTO dto, @RequestAttribute(name="session") SessionDTO session) throws IOException, SQLException{
		ResponseDTO response = new ResponseDTO();
		HttpStatus status = HttpStatus.OK;
		try {
			Object result = null;
			if(CommonUtil.isAccessAllowed(session, AccessType.DELETE, AccessModules.PRODUCTCONFIGURATION_ADMIN)) {
				result = productMemoryService.delete(dto, session);
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
	@RequestMapping(value = "/memory/list", method = RequestMethod.POST)
	public ResponseEntity list(@RequestAttribute(name="session") SessionDTO session, @RequestBody FiltersRequestDTO filters) throws IOException, SQLException{
		ResponseDTO response = new ResponseDTO();
		HttpStatus status = HttpStatus.OK;
		try {
			ResultDTO result = new ResultDTO();
			if(CommonUtil.isAccessAllowed(session, AccessType.READ, AccessModules.PRODUCTCONFIGURATION_ADMIN, AccessModules.PRODUCT_ADMIN, AccessModules.SELLERPRODUCT)) {
				result = productMemoryService.list(session, filters.getFilters());
			}else {
				result.setErrorDTO( new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have read access for this module."));
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
	@RequestMapping(value = "/memory/get", method = RequestMethod.GET)
	public ResponseEntity get(@RequestAttribute(name = "session") SessionDTO session, @RequestParam(name = "id", defaultValue = "0") Long id) throws IOException, SQLException{
		ResponseDTO response = new ResponseDTO();
		HttpStatus status = HttpStatus.OK;
		try {
			Object result = null;
			if(CommonUtil.isAccessAllowed(session, AccessType.READ, AccessModules.PRODUCTCONFIGURATION_ADMIN, AccessModules.PRODUCT_ADMIN, AccessModules.SELLERPRODUCT)) {
				result = productMemoryService.get(session, id);
			}else {
				result = new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have read access for this module.");
			}
			status = CommonUtil.fillResponseDTO(result, response, status);
		}catch(Exception e) {
			e.printStackTrace();
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			response.setStatus(ResponseStatus.FAILURE);
			response.setError(new ErrorDTO(status, "Error in get()", e.getMessage()));
			
		}
		return new ResponseEntity<>(response, status);
	}

}
