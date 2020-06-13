package com.tech.dream.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
import com.tech.dream.model.PagingSortSearchDTO;
import com.tech.dream.model.ProductInventoryItemDTO;
import com.tech.dream.model.ResponseDTO;
import com.tech.dream.model.ResultDTO;
import com.tech.dream.model.SearchQueryDTO;
import com.tech.dream.model.SessionDTO;
import com.tech.dream.service.product.ProductInventoryItemService;
import com.tech.dream.util.AccessType;
import com.tech.dream.util.CommonUtil;
import com.tech.dream.util.Constants.AccessModules;
import com.tech.dream.util.Constants.DisplayModuleNames;
import com.tech.dream.util.Constants.ResponseStatus;

@RestController
@RequestMapping(value="/api/sellerproduct/inventory/item")
public class ProductInventoryItemController {
	
	@Autowired
	private ProductInventoryItemService productInventoryItemService;
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public ResponseEntity create(@RequestBody ProductInventoryItemDTO dto, @RequestAttribute(name="session") SessionDTO session) throws IOException, SQLException{
		ResponseDTO response = new ResponseDTO();
		HttpStatus status = HttpStatus.CREATED;
		try {
			Object result = null;
			if(CommonUtil.isAccessAllowed(session, AccessType.CREATE, AccessModules.SELLERPRODUCT)) {
				result = productInventoryItemService.create(dto, session);
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
	public ResponseEntity update(@RequestBody ProductInventoryItemDTO dto, @RequestAttribute(name="session") SessionDTO session) throws IOException, SQLException{
		ResponseDTO response = new ResponseDTO();
		HttpStatus status = HttpStatus.OK;
		try {
			Object result = null;
			if(CommonUtil.isAccessAllowed(session, AccessType.UPDATE, AccessModules.SELLERPRODUCT)) {
				result = productInventoryItemService.update(dto, session);
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
	public ResponseEntity delete(@RequestBody ProductInventoryItemDTO dto, @RequestAttribute(name="session") SessionDTO session) throws IOException, SQLException{
		ResponseDTO response = new ResponseDTO();
		HttpStatus status = HttpStatus.OK;
		try {
			Object result = null;
			if(CommonUtil.isAccessAllowed(session, AccessType.DELETE, AccessModules.SELLERPRODUCT)) {
				result = productInventoryItemService.delete(dto, session);
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
			if(CommonUtil.isAccessAllowed(session, AccessType.READ, AccessModules.SELLERPRODUCT)) {
				result = productInventoryItemService.list(session, filters.getSellerProductId(), filters.getFilters());
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
			ResultDTO result = new ResultDTO();
			if(CommonUtil.isAccessAllowed(session, AccessType.READ, AccessModules.SELLERPRODUCT)) {
				PagingSortSearchDTO filters = new PagingSortSearchDTO();
				List<SearchQueryDTO> sDTOList = new ArrayList<>();
				SearchQueryDTO sDTO = new SearchQueryDTO();
				sDTO.setSearchField("id");
				sDTO.setSearchText(String.valueOf(id));
				sDTOList.add(sDTO);
				filters.setSearch(sDTOList);
				result = productInventoryItemService.list(session, null, filters);
				if(result.getData()!=null && ((List)result.getData()).size()>0) {
					result.setData(((List)result.getData()).get(0));
				}else {
					result.setData(null);
				}
			}else {
				result.setErrorDTO(new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have read access for this module."));
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
