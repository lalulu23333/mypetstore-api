package org.csu.mypetstore.api.service;

import org.csu.mypetstore.api.common.CommonResponse;
import org.csu.mypetstore.api.vo.CartItemVO;
import org.csu.mypetstore.api.vo.CartVO;

import java.util.List;

public interface CartService {

    // 根据userid获取该用户的购物车对象
    CartVO getCart(String username);

    // 登录时，合并当前购物车和持久化购物车
    CartVO mergeCarts(CartVO tempCart, CartVO persisCart);

    // 数量 += 1 或者新添加一个item
    CommonResponse addCartItem(CartVO cart, String itemId);

    // 数量 += plusQty
    CommonResponse addCartItem(CartVO cart, String itemId, int plusQty);

    // 根据itemId删除购物车对象
    CommonResponse deleteCartItem(CartVO cart, String itemId);

    // 根据itemId更新购物车对象数量为quantity
    CommonResponse updateCartItemQty(CartVO cart, int quantity, String itemId);

    CartItemVO getCartItem(int cartItemId);

    // 根据userid获取该用户的购物车内容列表
    List<CartItemVO> getCartItemsByUsername(String username);

    CommonResponse<CartVO> getCartRe(String username);
}
