package com.newview.orders.model;

import java.util.List;

public interface OrdersDAO {
	
	public int insert(OrdersVO OrdersVO);
    public int update(OrdersVO OrdersVO);
    public int delete(Integer orderID);
    public OrdersVO findByPrimaryKey(Integer orderID);
    public List<OrdersVO> getAll();
	int deleteOrdersbyUserIDandPubID(Integer userID, Integer pubID);
	public Integer getOrderBy(int userID, int pubID);
          
//  萬用複合查詢(傳入參數型態Map)(回傳List)
//  public List<OrdersVO> getAll(Map<String, String[]> map);
    
    
    
    //for Publisher Backstage get Order List  by Mandy
    public List<OrdersVO> getOrdersByPubID(Integer pubID);
    
}