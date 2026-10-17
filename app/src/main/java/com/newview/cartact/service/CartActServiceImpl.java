package com.newview.cartact.service;

import java.util.List;

import com.newview.cartact.model.CartActDAO;
import com.newview.cartact.model.CartActDAOImpl;
import com.newview.cartact.model.CartActVO;

public class CartActServiceImpl implements CartActService {

	private CartActDAO cartActdao;

	public CartActServiceImpl() {
		cartActdao = new CartActDAOImpl();
	}

	@Override
	public int addCart(CartActVO cartActVO) {
		return cartActdao.insert(cartActVO);
	}

	@Override
	public int updateCart(CartActVO cartActVO) {
		return cartActdao.update(cartActVO);
	}

	@Override
	public int deleteCart(int cartActID) {
		return cartActdao.delete(cartActID);
	}

	@Override
	public CartActVO getCartByPK(int cartActID) {
		return cartActdao.findeByPrimaryKey(cartActID);
	}

	@Override
	public List<CartActVO> getAll() {
		return cartActdao.getAll();
	}

}
