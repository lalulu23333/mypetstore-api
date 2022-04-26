package org.csu.mypetstore.api.controller;

import org.csu.mypetstore.api.common.CommonResponse;
import org.csu.mypetstore.api.entity.Order;
import org.csu.mypetstore.api.entity.OrderStatus;
import org.csu.mypetstore.api.service.CartService;
import org.csu.mypetstore.api.service.OrderService;
import org.csu.mypetstore.api.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private CartService cartService;

    // 根据用户名username获取orders入口列表(SimpleOrder)
    @GetMapping("/user/{username}/orders")
    @ResponseBody
    public CommonResponse<List<SimpleOrderVO>> getOrderListByUser(@PathVariable("username") String username) {
        System.out.println(username + orderService.getSimpleOrdersByUsername(username));
        CommonResponse<List<SimpleOrderVO>> commonResponse=orderService.getSimpleOrdersByUsername(username);
        return commonResponse;
    }

    // 根据orderId获取单个order的详细信息
    @GetMapping("/user/orders/{id}")
    @ResponseBody
    public CommonResponse<OrderVO> getOrderById(@PathVariable("id") String id) {
        System.out.println("id" + id);
        try {
            int orderId = Integer.parseInt(id);
            return orderService.getOrderById(orderId);
        } catch (Exception e) {
            return CommonResponse.createForArgument("订单编号参数id错误");
        }
    }

    @GetMapping("/newOrder/{username}")
    @ResponseBody
    public CommonResponse<OrderVO > login(
            @PathVariable("username") String username, HttpSession session) {
            System.out.println("用户登录成功"+username);
        CartVO tempCart = (CartVO) session.getAttribute("cart");
        session.setAttribute("cart", tempCart);
        OrderVO orderVO      =  orderService.getOrdersByUsernameList(username);
        CommonResponse<OrderVO> response = CommonResponse.createForSuccess(orderVO);
        return response;
    }
    @PostMapping("/confirmOrder")
    @ResponseBody
    public CommonResponse<OrderVO> confirmOrder(
            @RequestParam("username") String username,

            @RequestParam("expiryDate") String expiryDate,
            @RequestParam("billToFirstName") String billToFirstName,
            @RequestParam("creditCard") String creditCard,
            @RequestParam("billToLastName") String billToLastName,
            @RequestParam("billAddress1") String billAddress1,
            @RequestParam("billAddress2") String billAddress2,
            @RequestParam("billCity") String billCity,
            @RequestParam("billState") String billState,
            @RequestParam("billZip") String billZip,
            @RequestParam("billCountry") String billCountry,
            @RequestParam("cardType") String cardType,
            @RequestParam("shippingAddressRequired") Boolean shippingAddressRequired,
            @RequestParam(value = "shipToFirstName", required = false) String shipToFirstName,
            @RequestParam(value = "shipToLastName", required = false) String shipToLastName,
            @RequestParam(value = "shipAddress1", required = false) String shipAddress1,
            @RequestParam(value = "shipAddress2", required = false) String shipAddress2,
            @RequestParam(value = "shipCity", required = false) String shipCity,
            @RequestParam(value = "shipState", required = false) String shipState,
            @RequestParam(value = "shipZip", required = false) String shipZip,
            @RequestParam(value = "shipCountry", required = false) String shipCountry,
            HttpSession session) {
        System.out.println("用户登录成功"+expiryDate+billAddress1+billAddress2+billToFirstName+billToLastName
                +creditCard+billCity+billCountry+billState+billZip+cardType+shippingAddressRequired+shipToFirstName
                +shipAddress1+shipAddress2+shipToLastName+shipCity+shipState+shipZip+shipCountry);

        CartVO cartVO = (CartVO) session.getAttribute("cart");
        CartVO cart = cartService.mergeCarts(cartVO, null);
        Order order = new Order();
        order.setOrderId(orderService.getNextId("ordernum"));
        order.setUsername(username);
        order.setOrderDate(new Date());
        order.setBillAddress1(billAddress1);
        order.setBillAddress2(billAddress2);
        order.setBillCity(billCity);
        order.setBillState(billState);
        order.setBillZip(billZip);
        order.setBillCountry(billCountry);
        order.setCourier("UPS");
        order.setTotalPrice(cartVO.getSubTotal());
        order.setBillToFirstName(billToFirstName);
        order.setBillToLastName(billToLastName);
        order.setCardType(cardType);
        order.setCreditCard(creditCard);
        order.setExpiryDate(expiryDate);
        order.setLocale("CSU");

        OrderStatus orderstatus = new OrderStatus();
        orderstatus.setStatus("待发货");
        orderstatus.setTimestamp(order.getOrderDate());

        if (shippingAddressRequired) {
            order.setShipAddress1(shipAddress1);
            order.setShipAddress2(shipAddress2);
            order.setShipCity(shipCity);
            order.setShipState(shipState);
            order.setShipZip(shipZip);
            order.setShipCountry(shipCountry);
            order.setShipToFirstName(shipToFirstName);
            order.setShipToLastName(shipToLastName);
        } else {
            order.setShipAddress1(billAddress1);
            order.setShipAddress2(billAddress2);
            order.setShipCity(billCity);
            order.setShipState(billState);
            order.setShipZip(billZip);
            order.setShipCountry(billCountry);
            order.setShipToFirstName(billToFirstName);
            order.setShipToLastName(billToLastName);
        }
        List<LineItemVO> res = new ArrayList<>();
        OrderVO orderVO1 = orderService.orderToOrderVO(order,orderstatus,res);
        CommonResponse response = orderService.initOrder(cart, orderVO1);
        session.setAttribute("orderVO", response.getData());
        session.setAttribute("forward_order", orderVO1.getOrderId());
        return response;
    }
    @GetMapping("/confOrder")
    @ResponseBody
    public CommonResponse<OrderVO> confOrder(HttpSession session) {
        CommonResponse<OrderVO> response = CommonResponse.createForSuccess((OrderVO)session.getAttribute("orderVO"));
        return response;
    }




    @PatchMapping("/pay/status")
    @ResponseBody
    public CommonResponse paidOrder(HttpSession session) {
        int orderId = (int) session.getAttribute("forward_order");
        Map<String, CartItemVO> itemMap = ((CartVO) session.getAttribute("cart")).getItemMap();
        String[] strings = itemMap.keySet().toArray(new String[0]);
        CartVO cartVO = (CartVO) session.getAttribute("cart");
        for(int i=0;i<=itemMap.keySet().size();i++) {
            cartService.deleteCartItem(cartVO,strings[i]);
        }
        session.setAttribute("cart", cartVO);
        session.removeAttribute("orderVO");
        session.removeAttribute("forward_order");
        return orderService.payOrder(orderId);
    }
    @PostMapping("/error")
    @ResponseBody
    public CommonResponse orderError(@RequestParam("response") CommonResponse response) {
        return response;
    }

    //创建订单
//    @PostMapping("newOrder")
//    @ResponseBody
//    public CommonResponse insertOrder(@RequestParam("orderVO") OrderVO orderVO, HttpSession session){
//        AccountVO accountVO=(AccountVO)session.getAttribute("login_account");
//        cartService.clear(accountVO.getUsername());
//        OrderVO order=(OrderVO) orderService.initOrder(accountVO).getData();
//        order.setBillAddress1(orderVO.getBillAddress1());
//        order.setBillAddress2(orderVO.getBillAddress2());
//        order.setBillCity(orderVO.getBillCity());
//        order.setBillCountry(orderVO.getBillCountry());
//        order.setBillState(orderVO.getBillState());
//        order.setBillToFirstName(orderVO.getBillToFirstName());
//        order.setBillToLastName(orderVO.getBillToLastName());
//        order.setBillZip(orderVO.getBillZip());
//        order.setCardType(orderVO.getCardType());
//        order.setCourier(orderVO.getCourier());
//        order.setCreditCard(orderVO.getCreditCard());
//        order.setExpiryDate(orderVO.getExpiryDate());
//        order.setShipZip(orderVO.getShipZip());
//        order.setShipToFirstName(orderVO.getShipToFirstName());
//        order.setShipToLastName(orderVO.getShipToLastName());
//        order.setShipAddress1(orderVO.getShipAddress1());
//        order.setShipAddress2(orderVO.getShipAddress2());
//        order.setShipCity(orderVO.getShipCity());
//        order.setShipCountry(orderVO.getShipCountry());
//        order.setShipState(orderVO.getShipState());
//
//        session.setAttribute("orderVO",order);
//        session.removeAttribute("cartVO");
//        return orderService.insertOrder(order);
//    }

//
//    @GetMapping("undeliverOrders")
//    @ResponseBody
//    public CommonResponse<List<OrderStatus>> getUndeliveredOrder(){
//        return  orderService.getUndeliveredOrder();
//    }
//
//
//    @GetMapping("deliveredOrders")
//    @ResponseBody
//    public CommonResponse<List<OrderStatus>> getDeliveredOrder(){
//        return orderService.getDeliveredOrder();
//    }
//
//
//    @PostMapping("undeliverOrders/{orderId}/deliver")
//    @ResponseBody
//    public CommonResponse Deliver(@PathVariable("orderId") String orderId){
//        Integer Id=Integer.parseInt(orderId);
//        return orderService.Deliver(Id);
//    }
//
//
//    @PostMapping("editOrder")
//    @ResponseBody
//    public CommonResponse editOrder(@RequestParam("orderVO") OrderVO orderVO, HttpSession session){
//        CommonResponse response = orderService.editOrder(orderVO);
//        session.setAttribute("orderVO", response.getData());
//        return response;
//    }
//
//
//    @GetMapping("searchOrder")
//    @ResponseBody
//    public CommonResponse<List<Orders>> searchOrder(@RequestParam("keyword") String keyword, HttpSession session){
//        return orderService.searchOrder(keyword);
//    }
}
