package org.csu.mypetstore.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.csu.mypetstore.api.common.CommonResponse;
import org.csu.mypetstore.api.service.CartService;
import org.csu.mypetstore.api.vo.AccountVO;
import org.csu.mypetstore.api.vo.CartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Controller
@Slf4j
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/registerCart")
    @ResponseBody
    public CommonResponse<CartVO> registerCart(HttpSession session) {
        AccountVO account = (AccountVO) session.getAttribute("login_account");
        CartVO cart = account == null ? cartService.getCart(null) : cartService.getCart(account.getUsername());
        session.setAttribute("cart", cart);
        return CommonResponse.createForSuccess(cart);
    }

    @GetMapping("")
    @ResponseBody
    public CommonResponse<CartVO> viewCart(HttpSession session) {
        CartVO cart = (CartVO) session.getAttribute("cart");
        return CommonResponse.createForSuccess(cart);
    }

    @PostMapping("/items/{item_id}")
    @ResponseBody
    public CommonResponse addItemToCart(HttpSession session, @PathVariable("item_id") String itemId) {
        CartVO cartVO = (CartVO) session.getAttribute("cart");
        CartVO cart = cartService.mergeCarts(cartVO, null);
        CommonResponse response = cartService.addCartItem(cart, itemId);
        session.setAttribute("cart", cart);
        System.out.println(response);
        return response;
    }

    @DeleteMapping("/items/{item_id}")
    @ResponseBody
    public CommonResponse deleteItemFromCart(HttpSession session, @PathVariable("item_id") String itemId) {
        CartVO cartVO = (CartVO) session.getAttribute("cart");
        CartVO cart = cartService.mergeCarts(cartVO, null);
        CommonResponse response = cartService.deleteCartItem(cart, itemId);
        session.setAttribute("cart", cart);
        return response;
    }

    @PatchMapping("/items/{item_id}/quantity/{qty}")
    @ResponseBody
    public CommonResponse updateItemQtyFromCart(HttpSession session,
                                                @PathVariable("item_id") String itemId,
                                                @PathVariable String qty) {
        CartVO cartVO = (CartVO) session.getAttribute("cart");
        CartVO cart = cartService.mergeCarts(cartVO, null);
        int quantity;
        try {
            quantity = Integer.parseInt(qty);
        } catch (Exception e) {
            log.error("参数错误", e);
            return CommonResponse.createForArgument("数量参数qty错误");
        }
        cartService.updateCartItemQty(cart, quantity, itemId);
        session.setAttribute("cart", cart);

        return CommonResponse.createForSuccess(cart);
    }
}
