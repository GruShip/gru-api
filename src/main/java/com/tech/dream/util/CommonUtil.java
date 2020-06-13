package com.tech.dream.util;

import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import com.tech.dream.model.ErrorDTO;
import com.tech.dream.model.ResponseDTO;
import com.tech.dream.model.ResultDTO;
import com.tech.dream.model.SessionDTO;
import com.tech.dream.model.UserGroupAccessMappingDTO;
import com.tech.dream.util.Constants.CompanyType;
import com.tech.dream.util.Constants.DataType;
import com.tech.dream.util.Constants.ResponseStatus;
import com.tech.dream.util.Constants.SearchType;


public class CommonUtil {
	
	private static final int ITERATIONS = 65536;
	private static final int KEY_LENGTH = 512;
	private static final String ALGORITHM = "PBKDF2WithHmacSHA512";
	
	private static String salt = "test";

	public static String generateSalt(final int length) {
		return Base64.getEncoder().encodeToString(salt.getBytes());
	}
	
	public static String getHash(String password) throws Exception {
		if (StringUtils.isEmpty(salt)) {
			salt = generateSalt(20);
		}
		char[] chars = password.toCharArray();
		byte[] bytes = salt.getBytes();

		PBEKeySpec spec = new PBEKeySpec(chars, bytes, ITERATIONS, KEY_LENGTH);

		Arrays.fill(chars, Character.MIN_VALUE);

		SecretKeyFactory fac = SecretKeyFactory.getInstance(ALGORITHM);
		byte[] securePassword = fac.generateSecret(spec).getEncoded();
		spec.clearPassword();
		return Base64.getEncoder().encodeToString(securePassword);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static HttpStatus fillResponseDTO(Object result, ResponseDTO response, HttpStatus status) {
		if(result instanceof ErrorDTO) {
			ErrorDTO error = (ErrorDTO) result;
			status = error.getStatus();
			response.setError(error);
			response.setStatus(ResponseStatus.FAILURE);
		}else {
			response.setData(result);
			response.setStatus(ResponseStatus.SUCCESS);
		}
		return status;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static HttpStatus fillResponseDTO(ResultDTO result, ResponseDTO response, HttpStatus status) {
		ErrorDTO error = null;
		if (result.getData() != null){
            response.setStatus(ResponseStatus.SUCCESS);
        }else if(result.getErrorDTO()!=null) {
            error = (ErrorDTO) result.getErrorDTO();
            status = error.getStatus();
            response.setStatus(ResponseStatus.FAILURE);
        }
        response.setError(error);
        response.setData(result.getData());
		response.setTotalCount(result.getTotalCount());
		response.setPageNumber(result.getPageNumber());
		response.setPageSize(result.getPageSize());
        return status;
	}

	public static void main(String[] args) throws Exception {
		System.out.println(CommonUtil.getHash("test"));
	}
	
	public static boolean isAccessAllowed(SessionDTO session, AccessType type, long ...moduleIds) {
		boolean isAccessAllowed = false;
		for(long moduleId: moduleIds) {
			Map<Long, UserGroupAccessMappingDTO> accessModuleMap = session.getAccessModuleList();
			UserGroupAccessMappingDTO accessMappingDTO = accessModuleMap.get(Long.valueOf(moduleId));
			if(accessMappingDTO!=null) {
				switch(type) {
					case CREATE:
						isAccessAllowed = accessMappingDTO.getCreateAccess();
						break;
					case READ:
						isAccessAllowed = accessMappingDTO.getReadAccess();
						break;
					case UPDATE:
						isAccessAllowed = accessMappingDTO.getUpdateAccess();
						break;
					case DELETE:
						isAccessAllowed = accessMappingDTO.getDeleteAccess();
						break;
				}
			}
		}
		isAccessAllowed = true;
		return isAccessAllowed;
	}
	
	public static boolean isMarketPlaceCompany(SessionDTO session) {
		return CompanyType.MARKETPLACE.equalsIgnoreCase(session.getCompanyType());
	}

	public static String getSearchType(String searchType) {
		if(StringUtils.isEmpty(searchType)) {
			return SearchType.CONTAINS;
		}
		switch(searchType.toUpperCase()) {
			case SearchType.CONTAINS:
				return SearchType.CONTAINS;
			case SearchType.EQUALS:
				return SearchType.EQUALS;
			case SearchType.IN:
				return SearchType.IN;
			case SearchType.BETWEEN:
				return SearchType.BETWEEN;
			case SearchType.GREATERTHAN:
				return SearchType.GREATERTHAN;
			case SearchType.LESSTHAN:
				return SearchType.LESSTHAN;
			default:
				return SearchType.CONTAINS;
		}
	}
	
	public static String getSearchQueryField(String searchType, String fieldDatatype, String dbField, String searchText) {
		String query = "";
		switch (fieldDatatype) {
			case DataType.TYPE_STRING:
				switch(searchType) {
					case SearchType.CONTAINS:
						query = dbField + " LIKE " + "'%" + CommonUtil.escapeSql(searchText) + "%'";
						break;
					case SearchType.EQUALS:
						query = dbField + " = " + "'" + CommonUtil.escapeSql(searchText) + "'";
						break;
					case SearchType.IN:
						searchText = searchText.replace("#@#", "','");
						query = dbField + " IN ('" + CommonUtil.escapeSql(searchText) + "')";
						break;
				}	
				break;
			case DataType.TYPE_INT:
				switch(searchType) {
					case SearchType.CONTAINS:
					case SearchType.EQUALS:
						query = dbField + " = " + CommonUtil.escapeSql(searchText);
						break;
					case SearchType.IN:
						searchText = searchText.replace("#@#", ",");
						query = dbField + " IN (" + CommonUtil.escapeSql(searchText) + ")";
						break;
					case SearchType.GREATERTHAN:
						query = dbField + " > " + CommonUtil.escapeSql(searchText);
						break;
					case SearchType.LESSTHAN:
						query = dbField + " < " + CommonUtil.escapeSql(searchText);
						break;
					case SearchType.BETWEEN:
						String[] parts = searchText.split("#@#");
						query = dbField + " BETWEEN " + CommonUtil.escapeSql(parts[0]) + " and " + CommonUtil.escapeSql(parts[1]);
						break;
				}
				break;
		}
		return query;
	}
	
	public static String escapeSql(String str){
		  String data = null;
		  if (str != null && str.length() > 0) {
		    str = str.replace("\\", "\\\\");
		    str = str.replace("'", "\\'");
		    str = str.replace("\0", "\\0");
		    str = str.replace("\n", "\\n");
		    str = str.replace("\r", "\\r");
		    str = str.replace("\"", "\\\"");
		    str = str.replace("\\x1a", "\\Z");
		    data = str;
		  }
		return data;
	}
}
