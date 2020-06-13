package com.tech.dream.service.user;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tech.dream.db.entity.Company;
import com.tech.dream.db.entity.User;
import com.tech.dream.db.entity.UserCompanyAccessMapping;
import com.tech.dream.db.repository.UserCompanyAccessMappingRepository;

@Service
@Transactional
public class UserCompanyAccessMappingService {

	@Autowired
	private UserCompanyAccessMappingRepository repository;
	
	public void removeAllMappings(Long userId) {
		repository.removeAllMappings(userId);
	}
	
	public Object create(Long userId, List<Long> companyIdList) {
		if(companyIdList==null || companyIdList.size()==0) {
			repository.removeAllMappings(userId);
			return null;
		}
		
		repository.removeMappingApartFromGivenCompanyIdList(userId, companyIdList);
		List<UserCompanyAccessMapping> ucamList = new ArrayList<UserCompanyAccessMapping>();
		for(Long companyId: companyIdList) {
			if(repository.existsMappingByUserIdAndCompanyId(userId, companyId)>0) {
				continue;
			}
			UserCompanyAccessMapping ucam = new UserCompanyAccessMapping();
			ucam.setUser(new User(userId));
			if(0 != companyId.longValue()) {
				ucam.setCompany(new Company(companyId));
			}
			ucamList.add(ucam);
		}
		if(ucamList.size()>0) {
			repository.saveAll(ucamList);
		}
		return null;
	}

	public List<Long> findUserAccessCompanyIdList(Long userId) {
		List<Long> companyIdList = new ArrayList<>();
		List<Object[]> datalistResult = repository.findIdListByUserId(userId);
		if(datalistResult!=null && datalistResult.size()>0) {
			for(Object[] dataResult : datalistResult) {
				companyIdList.add(dataResult[0]!=null?((BigInteger)dataResult[0]).longValue():0);
			}
		}
		return companyIdList;
	}
	
	
}
