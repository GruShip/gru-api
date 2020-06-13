package com.tech.dream.service.company;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tech.dream.db.entity.Company;
import com.tech.dream.db.entity.ProductListingCompanyMapping;
import com.tech.dream.db.repository.ProductListingCompanyMappingRepository;
import com.tech.dream.model.ErrorDTO;
import com.tech.dream.model.SessionDTO;

@Service
@Transactional
public class ProductListingCompanyMappingService {
	
	@Autowired
	private ProductListingCompanyMappingRepository repository;

	public void removeAllMappings(Long sourceCompanyId) {
		repository.removeAllProductListingCompanyMapping(sourceCompanyId);
	}
	
	public Object createList(List<Long> destinationCompanyIdList, Long sourceCompanyId, SessionDTO session) {
		if(destinationCompanyIdList==null || destinationCompanyIdList.size()==0) {
			this.removeAllMappings(sourceCompanyId);
			return true;
		}
		
		if(destinationCompanyIdList.contains(0L)) {
			destinationCompanyIdList.clear();
			destinationCompanyIdList.add(0L);
		}
		
		List<ErrorDTO> errors = new ArrayList<ErrorDTO>();
		int index = 0;
		for(Long destinationCompanyId:destinationCompanyIdList) {
			ErrorDTO error = createValidation(sourceCompanyId, destinationCompanyId, session);
			if(error!=null) {
				error.setIndex(index);
				errors.add(error);
			}
			index++;
		}
		if(errors.size()>0) {
			return errors;
		}
		
		List<ProductListingCompanyMapping> entityList = new ArrayList<>();
		repository.removeProductListingCompanyMappingApartFromGivenDestCompanyIdList(sourceCompanyId, destinationCompanyIdList);
		for(Long destinationCompanyId:destinationCompanyIdList) {
			if(destinationCompanyId!=null && sourceCompanyId.longValue() == destinationCompanyId.longValue()) {
				continue;
			}
			List<Object[]> datalistResult = repository.getIdBySourceCompanyIdAndDestinationCompanyIdId(sourceCompanyId, destinationCompanyId);
			Long id = null;
			if(datalistResult!=null && datalistResult.size()>0) {
				Object[] result = datalistResult.get(0);
				if(result!=null && result.length>0) {
					id = result[0]!=null?((BigInteger)result[0]).longValue():0L;
				}
			}
			ProductListingCompanyMapping entity = convertToEntity(id, sourceCompanyId, destinationCompanyId);
			entityList.add(entity);
		}
		if(entityList.size()>0) {
			repository.saveAll(entityList);
		}
		//dto = convertToDTO(entity);
		return true;
	}

	private ProductListingCompanyMapping convertToEntity(Long id, Long sourceCompanyId, Long destinationCompanyId) {
		ProductListingCompanyMapping e = new ProductListingCompanyMapping();
		e.setId(id);
		e.setSourceCompany(new Company(sourceCompanyId));
		if(destinationCompanyId.longValue()>0) {
			e.setDestinationCompany(new Company(destinationCompanyId));
		}
		
		return e;
	}
	
	private ErrorDTO createValidation(Long sourceCompanyId, Long destinationCompanyId, SessionDTO session) {
		return null;
	}

	public List<Long> getProductListingCompanyIdList(Long sourceCompanyId) {
		List<Long> productListingCompanyIdList = new ArrayList<>();
		List<Object[]> datalistResult = repository.getProductListingCompanyIdList(sourceCompanyId);
		if(datalistResult!=null && datalistResult.size()>0) {
			for(Object[] result: datalistResult) {
				productListingCompanyIdList.add(result!=null && result[0]!=null?((BigInteger)result[0]).longValue():0);
			}
		}
		return productListingCompanyIdList;
	}
}
