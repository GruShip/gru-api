package com.tech.dream.service.company;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tech.dream.db.entity.Address;
import com.tech.dream.db.entity.City;
import com.tech.dream.db.entity.Country;
import com.tech.dream.db.entity.State;
import com.tech.dream.db.repository.AddressRepository;
import com.tech.dream.model.AddressDTO;
import com.tech.dream.model.ErrorDTO;

@Service
@Transactional
public class AddressService {
	
	@Autowired
	private AddressRepository repository;
	
	
	public Object process(AddressDTO dto) throws Exception{
		if(dto.getId()==null) {//create call
			return create(dto);
		}else {
			if(dto.getRemoved() == Boolean.TRUE ) {//delete call
				return delete(dto);
			}else {//update call
				return update(dto);
			}
		}
	}
	
	public Object create(AddressDTO dto) throws Exception{
		ErrorDTO error = createValidation(dto);
		if(error!=null) {
			return error;
		}
		Address entity = convertToEntity(dto);
		repository.save(entity);
		dto = convertToDTO(entity);
		return dto;
	}
	
	public Object update(AddressDTO dto) throws Exception{
		ErrorDTO error = updateValidation(dto);
		if(error!=null) {
			return error;
		}
		repository.update(dto.getId(), dto.getAddressLine1(), dto.getAddressLine2(), dto.getCityId(), dto.getStateId(), dto.getCountryId(), dto.getPincode(), dto.getAddressType());
		return dto;
	}
	
	public Object delete(AddressDTO dto) {
		ErrorDTO error = deleteValidation(dto);
		if (error != null) {
			return error;
		}
		repository.delete(dto.getId());
		return null;
	}
	
	
	private ErrorDTO createValidation(AddressDTO dto) {
		if(dto.getCountryId()==null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "CountryId is mandatory parameter and cannot be null.");
		}
		
		if(StringUtils.isEmpty(dto.getAddressLine1())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "AddressLine1 is mandatory parameter and cannot be null.");
		}
		return null;
	}
	
	private ErrorDTO updateValidation(AddressDTO dto) {
		if(StringUtils.isEmpty(dto.getId())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory and cannot be null.");
		}
		if(!repository.existsById(dto.getId())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Company does not exists for given id. id: " + dto.getId());
		}
		if(dto.getCountryId()==null) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "CountryId is mandatory parameter and cannot be null.");
		}
		if(StringUtils.isEmpty(dto.getAddressLine1())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "AddressLine1 is mandatory parameter and cannot be null.");
		}
		return null;
	}
	
	private ErrorDTO deleteValidation(AddressDTO dto) {
		if (StringUtils.isEmpty(dto.getId())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST, "Id is mandatory and cannot be null.");
		}
		if (!repository.existsById(dto.getId())) {
			return new ErrorDTO(HttpStatus.BAD_REQUEST,
					"Address does not exists for given id. id: " + dto.getId());
		}
		
		return null;
	}
	
	public Address convertToEntity(AddressDTO dto) {
		Address e = new Address();
		e.setAddressLine1(dto.getAddressLine1());
		e.setAddressLine2(dto.getAddressLine2());
		e.setCountry(new Country(dto.getCountryId()));
		if(dto.getStateId()!=null) {
			e.setState(new State(dto.getStateId()));
		}
		if(dto.getCityId()!=null) {
			e.setCity(new City(dto.getCityId()));
		}
		e.setPincode(dto.getPincode());
		e.setAddressType(dto.getAddressType());
		e.setRemoved(dto.getRemoved());
		return e;
	}
	
	public AddressDTO convertToDTO(Address e) {
		AddressDTO dto = new AddressDTO();
		dto.setId(e.getId());
		dto.setAddressLine1(e.getAddressLine1());
		dto.setAddressLine2(e.getAddressLine2());
		dto.setCityId(e.getCity()!=null?e.getCity().getId():null);
		dto.setStateId(e.getState()!=null?e.getState().getId():null);
		dto.setCountryId(e.getCountry().getId());
		dto.setPincode(e.getPincode());
		dto.setAddressType(e.getAddressType());
		return dto;
	}

}
