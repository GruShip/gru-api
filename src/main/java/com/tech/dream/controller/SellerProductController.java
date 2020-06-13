package com.tech.dream.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.tech.dream.model.ErrorDTO;
import com.tech.dream.model.FileUploadDTO;
import com.tech.dream.model.FiltersRequestDTO;
import com.tech.dream.model.PagingSortSearchDTO;
import com.tech.dream.model.ResponseDTO;
import com.tech.dream.model.ResultDTO;
import com.tech.dream.model.SearchQueryDTO;
import com.tech.dream.model.SellerProductAssetMappingDTO;
import com.tech.dream.model.SellerProductDTO;
import com.tech.dream.model.SessionDTO;
import com.tech.dream.service.product.SellerProductService;
import com.tech.dream.util.AccessType;
import com.tech.dream.util.CommonUtil;
import com.tech.dream.util.Constants.AccessModules;
import com.tech.dream.util.Constants.DisplayModuleNames;
import com.tech.dream.util.Constants.ResponseStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value="/api/sellerproduct")
public class SellerProductController {
	
	@Autowired
	private SellerProductService sellerProductService;
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public ResponseEntity create(@RequestBody SellerProductDTO dto, @RequestAttribute(name="session") SessionDTO session) throws IOException, SQLException{
		ResponseDTO response = new ResponseDTO();
		HttpStatus status = HttpStatus.CREATED;
		try {
			Object result = null;
			if(CommonUtil.isAccessAllowed(session, AccessType.CREATE, AccessModules.SELLERPRODUCT)) {
				result = sellerProductService.create(dto, session);
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
	public ResponseEntity update(@RequestBody SellerProductDTO dto, @RequestAttribute(name="session") SessionDTO session) throws IOException, SQLException{
		ResponseDTO response = new ResponseDTO();
		HttpStatus status = HttpStatus.OK;
		try {
			Object result = null;
			if(CommonUtil.isAccessAllowed(session, AccessType.UPDATE, AccessModules.SELLERPRODUCT)) {
				result = sellerProductService.update(dto, session);
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
	@RequestMapping(value = "/status", method = RequestMethod.PUT)
	public ResponseEntity status(@RequestBody SellerProductDTO dto, @RequestAttribute(name="session") SessionDTO session) throws IOException, SQLException{
		ResponseDTO response = new ResponseDTO();
		HttpStatus status = HttpStatus.OK;
		try {
			Object result = null;
			if(CommonUtil.isAccessAllowed(session, AccessType.UPDATE, AccessModules.SELLERPRODUCT)) {
				result = sellerProductService.status(dto, session);
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
	@RequestMapping(value = "/approved", method = RequestMethod.PUT)
	public ResponseEntity approved(@RequestBody SellerProductDTO dto, @RequestAttribute(name="session") SessionDTO session) throws IOException, SQLException{
		ResponseDTO response = new ResponseDTO();
		HttpStatus status = HttpStatus.OK;
		try {
			Object result = null;
			if(CommonUtil.isAccessAllowed(session, AccessType.UPDATE, AccessModules.SELLERPRODUCT)) {
				result = sellerProductService.updateApproved(dto, session);
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
	public ResponseEntity delete(@RequestBody SellerProductDTO dto, @RequestAttribute(name="session") SessionDTO session) throws IOException, SQLException{
		ResponseDTO response = new ResponseDTO();
		HttpStatus status = HttpStatus.OK;
		try {
			Object result = null;
			if(CommonUtil.isAccessAllowed(session, AccessType.DELETE, AccessModules.SELLERPRODUCT)) {
				result = sellerProductService.delete(dto, session);
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
				result = sellerProductService.list(session, filters.getCompanyId(), filters.getFilters());
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
				result = sellerProductService.list(session, null, filters);
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/upload/asset", method = RequestMethod.POST,consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity uploadAsset(@RequestAttribute(name="session") SessionDTO session, 
										MultipartFile file1, String file1Type, String file1Url, 
										MultipartFile file2, String file2Type, String file2Url,
										MultipartFile file3, String file3Type, String file3Url,
										MultipartFile file4, String file4Type, String file4Url,
										MultipartFile file5, String file5Type, String file5Url,
										MultipartFile file6, String file6Type, String file6Url,
										Long id) throws IOException, SQLException{
		ResponseDTO response = new ResponseDTO();
		HttpStatus status = HttpStatus.CREATED;
		try {
			ResultDTO result = new ResultDTO();
			if(CommonUtil.isAccessAllowed(session, AccessType.CREATE, AccessModules.SELLERPRODUCT)) {

				List<FileUploadDTO> fileUploadDTOs = new ArrayList<FileUploadDTO>();
				if (file1 != null) {
					fileUploadDTOs.add(new FileUploadDTO(file1, null, file1Type, "FILE"));
				} else if(!StringUtils.isEmpty(file1Url)) {
					fileUploadDTOs.add(new FileUploadDTO(null, file1Url, file1Type, "URL"));
				}

				if (file2 != null) {
					fileUploadDTOs.add(new FileUploadDTO(file2, null, file2Type, "FILE"));
				} else if(!StringUtils.isEmpty(file2Url)) {
					fileUploadDTOs.add(new FileUploadDTO(null,file2Url, file2Type, "URL"));
				}

				if (file3 != null) {
					fileUploadDTOs.add(new FileUploadDTO(file3, null, file3Type, "FILE"));
				} else if(!StringUtils.isEmpty(file3Url)) {
					fileUploadDTOs.add(new FileUploadDTO(null,file3Url, file3Type, "URL"));
				}

				if (file4 != null) {
					fileUploadDTOs.add(new FileUploadDTO(file4, null, file4Type, "FILE"));
				} else if(!StringUtils.isEmpty(file4Url)) {
					fileUploadDTOs.add(new FileUploadDTO(null,file4Url, file4Type, "URL"));
				}

				if (file5 != null) {
					fileUploadDTOs.add(new FileUploadDTO(file5, null, file5Type, "FILE"));
				} else if(!StringUtils.isEmpty(file5Url)) {
					fileUploadDTOs.add(new FileUploadDTO(null,file5Url, file5Type, "URL"));
				}

				if (file6 != null) {
					fileUploadDTOs.add(new FileUploadDTO(file6, null, file6Type, "FILE"));
				} else if(!StringUtils.isEmpty(file6Url)) {
					fileUploadDTOs.add(new FileUploadDTO(null,file6Url, file6Type, "URL"));
				}

				result = sellerProductService.uploadAsset(session, id, fileUploadDTOs);
			}else {
				result.setErrorDTO(new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have create access for this module."));
			}
			status = CommonUtil.fillResponseDTO(result, response, status);
		}catch(Exception e) {
			e.printStackTrace();
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			response.setStatus(ResponseStatus.FAILURE);
			response.setError(new ErrorDTO(status, "Error in uploadAsset()", e.getMessage()));
		}
		
		return new ResponseEntity<>(response, status);
	}

	@RequestMapping(value = "/delete/asset", method = RequestMethod.DELETE)
	public ResponseEntity deleteAsset(@RequestAttribute(name="session") SessionDTO session, @RequestBody List<SellerProductAssetMappingDTO> sellerProductAssetMappingDTOs) throws IOException, SQLException{
		ResponseDTO response = new ResponseDTO();
		HttpStatus status = HttpStatus.CREATED;
		try {
			ResultDTO result = new ResultDTO();
			if(CommonUtil.isAccessAllowed(session, AccessType.DELETE, AccessModules.SELLERPRODUCT)) {
				result = sellerProductService.deleteAsset(session, sellerProductAssetMappingDTOs);
			}else {
				result.setErrorDTO(new ErrorDTO(HttpStatus.FORBIDDEN, DisplayModuleNames.USER+" does not have create access for this module."));
			}
			status = CommonUtil.fillResponseDTO(result, response, status);
		}catch(Exception e) {
			e.printStackTrace();
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			response.setStatus(ResponseStatus.FAILURE);
			response.setError(new ErrorDTO(status, "Error in deleteAsset()", e.getMessage()));
		}
		
		return new ResponseEntity<>(response, status);
	}

}
