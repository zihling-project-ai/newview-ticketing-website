package com.newview.report.model;

import java.util.List;


public interface ReportDAO {
	public int insert(ReportVO reportVO);
	public int update(ReportVO reportVO);
	public int delete(Integer reportID);
	public ReportVO findeByPrimaryKey(Integer reportID);
	public List<ReportVO> getAll();
//	 萬用複合查詢(傳入參數型態Map)(回傳List)
//	public List<ReportVO> getAll(Map<String, String[]> map);

}
