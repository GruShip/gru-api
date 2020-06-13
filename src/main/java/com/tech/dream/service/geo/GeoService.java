package com.tech.dream.service.geo;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tech.dream.db.repository.GeoRepository;
import com.tech.dream.model.CityDTO;
import com.tech.dream.model.CountryDTO;
import com.tech.dream.model.StateDTO;

@Service
@Transactional
public class GeoService {
	
	@Autowired
	private GeoRepository repository;

	public List<CountryDTO> findAllCountry(){
		List<CountryDTO> list = new ArrayList<CountryDTO>();
		List<Object[]> resultSet = repository.findAllCountry();
		
		for(Object[] result:resultSet) {
			CountryDTO dto = new  CountryDTO();
			dto.setId(((BigInteger)result[0]).longValue());
			dto.setName((String)result[1]);
			dto.setShortCode((String)result[2]);
			dto.setPhoneCode((String)result[3]);
			list.add(dto);
		}
		return list;
	}

	public List findAllState(Long countryId) {
		List<StateDTO> list = new ArrayList<StateDTO>();
		List<Object[]> resultSet = repository.findAllState(countryId);
		
		for(Object[] result:resultSet) {
			StateDTO dto = new  StateDTO();
			dto.setId(((BigInteger)result[0]).longValue());
			dto.setName((String)result[1]);
			dto.setCountryId(((BigInteger)result[2]).longValue());
			list.add(dto);
		}
		return list;
	}

	public List findAllCity(Long stateId) {
		List<CityDTO> list = new ArrayList<CityDTO>();
		List<Object[]> resultSet = repository.findAllCity(stateId);
		
		for(Object[] result:resultSet) {
			CityDTO dto = new  CityDTO();
			dto.setId(((BigInteger)result[0]).longValue());
			dto.setName((String)result[1]);
			dto.setStateId(((BigInteger)result[2]).longValue());
			list.add(dto);
		}
		return list;
	}
	
}
