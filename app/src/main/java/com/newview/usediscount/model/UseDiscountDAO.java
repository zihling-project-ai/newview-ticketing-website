package com.newview.usediscount.model;

import java.util.*;

public interface UseDiscountDAO {

	public int insert(UseDiscountVO useDiscountVO);
	public int update(UseDiscountVO useDiscountVO);
	public int delete(Integer useDisID);
	public UseDiscountVO findeByPrimaryKey(Integer useDisID);
	public List<UseDiscountVO> getAll();
//	 萬用複合查詢(傳入參數型態Map)(回傳List)
//	public List<UseDiscountVO> getAll(Map<String, String[]> map);
	public int getUseDisIDBy(int discountNO, int userID);
	UseDiscountVO getUseDiscountByUserID(int userID);
}
