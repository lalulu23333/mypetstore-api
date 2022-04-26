package org.csu.mypetstore.api.controller;

import org.csu.mypetstore.api.common.CommonResponse;
import org.csu.mypetstore.api.common.ResponseCode;
import org.csu.mypetstore.api.service.AccountService;
import org.csu.mypetstore.api.service.CartService;
import org.csu.mypetstore.api.service.OrderService;
import org.csu.mypetstore.api.vo.AccountVO;
import org.csu.mypetstore.api.vo.CartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private AccountService accountService;
    @Autowired
    private CartService cartService;

    @PostMapping("/login")
    @ResponseBody
    public CommonResponse<AccountVO> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpSession session) {
        CommonResponse<AccountVO> response = accountService.getAccount(username, password);
        CartVO newCart, tempCart = (CartVO) session.getAttribute("cart");
        if (response.isSuccess()) {
            session.setAttribute("login_account", response.getData());
            System.out.println("用户登录成功");
            // 如果当前没有临时购物车则直接以数据库查出来的持久化购物车为临时购物车
            // 如果有，则进行购物车合并。具体判断位于mergeCarts方法体中
            newCart = cartService.mergeCarts(tempCart, cartService.getCart(username));
        } else {
            // tempCart==null则返回new Cart()，否则返回tempCart
            newCart = cartService.mergeCarts(tempCart, null);
        }
        session.setAttribute("cart", newCart);
        return response;
    }
    @PostMapping("/newOrdsadfgaweer")
    @ResponseBody
    public CommonResponse<AccountVO> logino(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpSession session) {
        CommonResponse<AccountVO> response = accountService.getAccount(username, password);
        CartVO newCart, tempCart = (CartVO) session.getAttribute("cart");
        if (response.isSuccess()) {
            session.setAttribute("login_account", response.getData());
            System.out.println("用户登录成功");
            // 如果当前没有临时购物车则直接以数据库查出来的持久化购物车为临时购物车
            // 如果有，则进行购物车合并。具体判断位于mergeCarts方法体中
            newCart = cartService.mergeCarts(tempCart, cartService.getCart(username));
        } else {
            // tempCart==null则返回new Cart()，否则返回tempCart
            newCart = cartService.mergeCarts(tempCart, null);
        }
        session.setAttribute("cart", newCart);
        return response;
    }
    @PostMapping("/get_login_account_info")
    @ResponseBody
    public CommonResponse<AccountVO> getLoginAccountInfo(HttpSession session) {
        AccountVO loginAccount = (AccountVO) session.getAttribute("login_account");
        if (loginAccount != null) {
            System.out.println("用户yi登录");
            return CommonResponse.createForSuccess(loginAccount);

        } else {
            System.out.println("用户未登录");
            return CommonResponse.createForError(ResponseCode.ERROR.getCode(),"用户未登录，无法获取用户信息");
        }
    }
    @GetMapping("/myAccount")
    @ResponseBody
    public CommonResponse<AccountVO> getmyAccount(HttpSession session) {
        AccountVO loginAccount = (AccountVO) session.getAttribute("login_account");
        if (loginAccount != null) {
            return CommonResponse.createForSuccess(loginAccount);
        } else {
            return CommonResponse.createForError(ResponseCode.ERROR.getCode(),"用户未登录，无法获取用户信息");
        }
    }

    @PostMapping("/signoff")
    @ResponseBody
    public CommonResponse logout(HttpSession session) {
        session.setAttribute("login_account", null);
        // 上线合并购物车就进行了持久化，登录状态下每添一个商品也会进行持久化
        // 退出则无需持久化购物车，只需重置购物车即可
        session.setAttribute("cart", new CartVO());
        System.out.println("用户已经登出");
        return CommonResponse.createForSuccessMessage("true");
    }

    @PostMapping("signup")
    @ResponseBody
    public CommonResponse register(//注册
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpSession session) {
        // 为了优化体验，注册只需填写用户名与密码，其余信息为默认值，可后期补充
        CommonResponse isExist = accountService.usernameExist(username);
        if (isExist.getMsg().equals("用户名不存在")) {
            CommonResponse response = accountService.insertAccount(username, password);
            AccountVO account = accountService.getAccount(username).getData();
            session.setAttribute("login_account", account);
            return response;
        } else if (isExist.getMsg().equals("用户名已存在")){
            return CommonResponse.createForError("用户名已存在");
        } else {
            return CommonResponse.createForError("服务器异常");
        }
    }

    @PutMapping("update")
    @ResponseBody
    public CommonResponse update(AccountVO accountVO, HttpSession session) {
        // 更新用户信息
        CommonResponse response = accountService.updateAccount(accountVO);
        session.setAttribute("login_account", response.getData());
        System.out.println("UPDATE"+response.getData());
        return response;
    }

    @PutMapping("/password")
    @ResponseBody
    public CommonResponse resetPassword(@RequestParam("username") String username,
                                        @RequestParam("curPw") String curPw,
                                        @RequestParam("newPw") String newPw) {
        // 密码修改 / 重置
        CommonResponse isExist = accountService.usernameExist(username);
        if (isExist.getMsg().equals("用户名已存在")) {
            return accountService.updatePassword(username, curPw, newPw);
        } else {
            return CommonResponse.createForError("用户名不存在");
        }
    }

    @PostMapping("/user_exist")
    @ResponseBody
    public CommonResponse userExist(@RequestParam("username") String username) {
        // 判断用户名是否已存在
        return accountService.usernameExist(username);
    }
//    @PostMapping("sendCode")
//    @ResponseBody
//    public CommonResponse<String> sendCode(@RequestParam("memPhone") String memPhone, HttpServletRequest request) throws Exception{
//        HttpSession session = request.getSession();
//        String apiUrl =
//        String appId =
//        String appSecret =
//        client = new Client(apiUrl, appId, appSecret);
//        Map<String, Object> params = new HashMap<String, Object>();
//        params.put("number", memPhone);
//        params.put("templateId", "8519");
//        int randomNum = (int)(Math.random()*(9999-1000+1))+1000;
//        String strRandom = Integer.toString(randomNum);
//        session.setAttribute("strRandom",strRandom);
//        String[] templateParams = new String[1];
//        templateParams[0] = strRandom;
//        params.put("templateParams", templateParams);
//        String result = client.send(params);
//        System.out.println(result);
//        if(result.equals("0")){
//            CommonResponse<String> response = CommonResponse.createForSuccessMessage("发送失败");
//            return response;
//        }else{
//            CommonResponse<String> response = CommonResponse.createForSuccess(strRandom);
//            return response;
//        }
//    }
//
//
//    @GetMapping("orderform")
//    @ResponseBody
//    public CommonResponse<List<Orders>> getOrderListbyUsername(HttpSession session){
//        String username=(String) session.getAttribute("username");
//        if (username != null) {
//            System.out.println("success");
//            CommonResponse<List<Orders>> response = orderService.getOrderListbyUsername(username);
//            return response;
//        } else {
//            System.out.println("null"+new Date());
//            return CommonResponse.createForError("用户未登录，不能获取用户订单");
//        }
//    }

}
