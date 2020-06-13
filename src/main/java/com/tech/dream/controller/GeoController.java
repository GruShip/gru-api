package com.tech.dream.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tech.dream.model.ErrorDTO;
import com.tech.dream.model.ResponseDTO;
import com.tech.dream.service.geo.GeoService;
import com.tech.dream.util.CommonUtil;
import com.tech.dream.util.Constants.ResponseStatus;

@RestController
@RequestMapping(value="/api")
public class GeoController {

	@Autowired
	private GeoService countryService;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/country/all", method = RequestMethod.GET)
	public ResponseEntity findAllCountry() throws IOException, SQLException{
		ResponseDTO response = new ResponseDTO();
		HttpStatus status = HttpStatus.OK;
		try {
			List result = countryService.findAllCountry();
			status = CommonUtil.fillResponseDTO(result, response, status);
		}catch(Exception e) {
			e.printStackTrace();
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			response.setStatus(ResponseStatus.FAILURE);
			response.setError(new ErrorDTO(status, "Error in findAll()", e.getMessage()));
		}
		
		return new ResponseEntity<>(response, status);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/state/all", method = RequestMethod.GET)
	public ResponseEntity findAllState(@RequestParam(name = "country_id", required = false, defaultValue = "0") Long countryId) throws IOException, SQLException{
		ResponseDTO response = new ResponseDTO();
		HttpStatus status = HttpStatus.OK;
		try {
			List result = countryService.findAllState(countryId);
			status = CommonUtil.fillResponseDTO(result, response, status);
		}catch(Exception e) {
			e.printStackTrace();
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			response.setStatus(ResponseStatus.FAILURE);
			response.setError(new ErrorDTO(status, "Error in findAll()", e.getMessage()));
		}
		
		return new ResponseEntity<>(response, status);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/city/all", method = RequestMethod.GET)
	public ResponseEntity findAllCity(@RequestParam(name = "state_id", required = false, defaultValue = "0") Long stateId) throws IOException, SQLException{
		ResponseDTO response = new ResponseDTO();
		HttpStatus status = HttpStatus.OK;
		try {
			List result = countryService.findAllCity(stateId);
			status = CommonUtil.fillResponseDTO(result, response, status);
		}catch(Exception e) {
			e.printStackTrace();
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			response.setStatus(ResponseStatus.FAILURE);
			response.setError(new ErrorDTO(status, "Error in findAll()", e.getMessage()));
		}
		
		return new ResponseEntity<>(response, status);
	}
	
}
