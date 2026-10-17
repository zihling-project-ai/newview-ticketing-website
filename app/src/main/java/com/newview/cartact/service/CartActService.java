package com.newview.cartact.service;

import java.util.List;

import com.newview.cartact.model.CartActVO;


public interface CartActService {
	
	int addCart(CartActVO cartActVO);

	int updateCart(CartActVO cartActVO);

	int deleteCart(int cartActID);

	CartActVO getCartByPK(int cartActID);

	List<CartActVO> getAll();
}
