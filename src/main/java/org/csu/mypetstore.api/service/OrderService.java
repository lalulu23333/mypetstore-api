package org.csu.mypetstore.api.service;

import org.csu.mypetstore.api.common.CommonResponse;
import org.csu.mypetstore.api.entity.Order;
import org.csu.mypetstore.api.entity.OrderStatus;
import org.csu.mypetstore.api.vo.CartVO;
import org.csu.mypetstore.api.vo.LineItemVO;
import org.csu.mypetstore.api.vo.OrderVO;
import org.csu.mypetstore.api.vo.SimpleOrderVO;

import java.util.List;

public interface OrderService {

    // 根据orderId获取一个订单全部信息
    CommonResponse<OrderVO> getOrderById(int orderId);

    // 获取所有订单的全部信息
    CommonResponse<List<OrderVO> > getOrders();

    // 获取所有订单的简略信息
    CommonResponse<List<SimpleOrderVO> > getSimpleOrders();

    // 根据用户userid获取所属订单的全部信息
    CommonResponse<List<OrderVO> > getOrdersByUsername(String username);

    // 根据用户userid获取所属订单的简略信息
    CommonResponse<List<SimpleOrderVO> > getSimpleOrdersByUsername(String username);

    // 根据订单id获取订单内所有商品的全部信息
    CommonResponse<List<LineItemVO>> getLineItems(int orderId);

    // 根据用户的购物车cart和前端传入的orderVO对象(只初始化了部分字段)
    // 生成一个OrderVO订单(初始化全部字段)并将其持久化，返回已被初始化的orderVO
    CommonResponse<OrderVO> initOrder(CartVO cart, OrderVO orderVO);
    OrderVO orderToOrderVO(Order order, OrderStatus orderStatus, List<LineItemVO> lineItems);
    int getNextId(String name);
    OrderVO getOrdersByUsernameList(String username);
    // 支付成功后修改订单状态status为已支付
    CommonResponse payOrder(int orderId);
}
