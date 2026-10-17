package com.newview.compic.model;

import java.util.List;


public interface ComPicDAO {
	public int insert(ComPicVO comPicVO);
	public int update(ComPicVO comPicVO);
	public int delete(Integer comPicVO);
	public ComPicVO findeByPrimaryKey(Integer comPicVO);
	public List<ComPicVO> getAll();
//	 萬用複合查詢(傳入參數型態Map)(回傳List)
//	public List<ComPicVO> getAll(Map<String, String[]> map);

}
