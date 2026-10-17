package com.newview.cartact.model;

import java.util.List;


public interface CartActDAO {
	public int insert(CartActVO cartActVO);
	public int update(CartActVO cartActVO);
	public int delete(Integer cartActVO);
	public CartActVO findeByPrimaryKey(Integer cartActID);
	public List<CartActVO> getAll();
//	 萬用複合查詢(傳入參數型態Map)(回傳List)
//	public List<CartActVO> getAll(Map<String, String[]> map);

}
