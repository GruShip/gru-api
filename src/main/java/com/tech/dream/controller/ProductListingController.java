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
import org.springframework.web.bind.annotation.RestController;

import com.tech.dream.model.ErrorDTO;
import com.tech.dream.model.FiltersRequestDTO;
import com.tech.dream.model.ResponseDTO;
import com.tech.dream.model.ResultDTO;
import com.tech.dream.model.SessionDTO;
import com.tech.dream.service.product.ProductListingService;
import com.tech.dream.util.AccessType;
import com.tech.dream.util.CommonUtil;
import com.tech.dream.util.Constants.AccessModules;
import com.tech.dream.util.Constants.DisplayModuleNames;
import com.tech.dream.util.Constants.ResponseStatus;

@RestController
@RequestMapping(value="/api/marketplace/product/")
public class ProductListingController {

	@Autowired
	private ProductListingService productListingService;
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public ResponseEntity list(@RequestAttribute(name="session") SessionDTO session, @RequestBody FiltersRequestDTO filters) throws IOException, SQLException{
		ResponseDTO response = new ResponseDTO();
		HttpStatus status = HttpStatus.OK;
		try {
			ResultDTO result = new ResultDTO();
			if(CommonUtil.isAccessAllowed(session, AccessType.READ, AccessModules.MARKETPLACE_PRODUCT)) {
				result = productListingService.list(session, filters.getCompanyId(), filters.getFilters());
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
	
}
