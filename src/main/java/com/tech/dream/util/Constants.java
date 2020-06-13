package com.tech.dream.util;

public interface Constants {

	public static final String MYSQL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String TZ_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.S'Z'";
	public static final Long DEFAULT_PAGE_SIZE = 25L;
	public static final Long DEFAULT_PAGE_NUMBER = 0L;

	public class ResponseStatus {
		public static final String SUCCESS = "Success";
		public static final String FAILURE = "Failure";
	}

	public class UserType {
		public static final String GENERAL = "GENERAL";
		public static final String FIELD_AGENT = "FIELD_AGENT";
	}

	public class CompanyType {
		public static final String SELLER = "SELLER";
		public static final String MARKETPLACE = "MARKETPLACE";
	}
	
	public class CompanyLevel {
		public static final String DISTRIBUTER = "DISTRIBUTER";
		public static final String WHOLESALER = "WHOLESALER";
		public static final String DEALER = "DEALER";
		public static final String RETAILER = "RETAILER";
	}

	public class DisplayModuleNames {
		public static final String COMPANY = "Company";
		public static final String COMPANYBRANCH = "CompanyBranch";
		public static final String USER = "User";
		public static final String USERGROUP = "UserGroup";
		public static final String PRODUCTCATEGORY = "ProductCategory";
		public static final String PRODUCTSUBCATEGORY = "ProductSubCategory";
		public static final String PRODUCTBRAND = "ProductBrand";
		public static final String PRODUCTTYPE = "ProductType";
		public static final String PRODUCTMODEL = "ProductModel";
		public static final String SYSTEMPRODUCT = "SystemProduct";
		public static final String PRODUCT = "Product";
		public static final String SELLERPRODUCT = "SellerProduct";
		public static final String PRODUCTCOLOR = "ProductColor";
		public static final String PRODUCTMEMORY = "ProductMemory";
		public static final String PRODUCTSTORAGE = "ProductStorage";
		public static final String PRODUCTINVENTORYITEM = "ProductInventoryItem";
		public static final String PRODUCTSCREENSIZE = "ProductScreenSize";
		public static final String PRODUCTTAXRATE = "ProductTaxRate";
		public static final String ORDER = "Order";
		public static final String PRODUCTCOUPON = "ProductCoupon";
	}

	public class AccessModules {
		public static final long COMPANY_ADMIN = 1;
		public static final long COMPANY_CLIENT = 6;
		public static final long COMPANYBRANCH = 2;
		public static final long USER = 3;
		public static final long USERGROUP = 4;
		public static final long PRODUCTCONFIGURATION_ADMIN = 5;
		public static final long PRODUCT_ADMIN = 7;
		public static final long SELLERPRODUCT = 8;
		public static final long MARKETPLACE_PRODUCT = 9;
		public static final long ORDER = 10;
		public static final long PRODUCTCOUPON = 11;
	}

	public class DataType {
		public static final String TYPE_INT = "int";
		public static final String TYPE_STRING = "string";
		public static final String TYPE_BOOL = "bool";
	}
	
	public class OrderType {
		public static final String SALES = "SALES";
		public static final String PURCHASE = "PURCHASE";
	}
	
	public class OrderStatus {
		public static final String CREATED = "CREATED";
		public static final String ACCEPTED = "ACCEPTED";
		public static final String REJECTED = "REJECTED";
		public static final String CANCELLED = "CANCELLED";
		public static final String INTRANSIT = "INTRANSIT";
		public static final String DELIVERED = "DELIVERED";
		public static final String NOTDELIVERED = "NOTDELIVERED";
		public static final String RETURNED = "RETURNED";
	}
	
	public class SearchType{
		public static final String CONTAINS = "CONTAINS";
		public static final String EQUALS = "EQUALS";
		public static final String IN = "IN";
		public static final String BETWEEN = "BETWEEN";
		public static final String GREATERTHAN = "GREATERTHAN";
		public static final String LESSTHAN = "LESSTHAN";
	}

	static enum OrderStates {
		CREATED, ACCEPTED, REJECTED, CANCELLED, INTRANSIT, DELIVERED, NOTDELIVERED, RETURNED
	}

	static enum OrderEvents {
		SELLER_ACCEPT, SELLER_REJECT, ORDER_CANCELLED, ORDER_INTRANSIT, ORDER_DELIVERED, ORDER_NOTDELIVERED, ORDER_RETURNED
	}

}
