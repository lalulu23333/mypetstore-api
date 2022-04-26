package org.csu.mypetstore.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.csu.mypetstore.api.common.CommonResponse;
import org.csu.mypetstore.api.entity.*;
import org.csu.mypetstore.api.persistence.*;
import org.csu.mypetstore.api.service.OrderService;
import org.csu.mypetstore.api.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service("orderService")
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderStatusMapper orderStatusMapper;
    @Autowired
    private LineItemMapper lineItemMapper;
    @Autowired
    private SequenceMapper sequenceMapper;

    @Override
    public CommonResponse<OrderVO> getOrderById(int orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return CommonResponse.createForError("不存在该订单(数据库查无数据)");
        }

        OrderStatus orderStatus = orderStatusMapper.selectById(orderId);
        if (orderStatus == null) {
            return CommonResponse.createForError("查询出错：orderstatus无数据");
        }

        List<LineItemVO> lineItemVOList = getLineItems(orderId).getData();
        OrderVO orderVO = orderToOrderVO(order, orderStatus, lineItemVOList);

        return CommonResponse.createForSuccess(orderVO);
    }

    @Override
    public CommonResponse<List<OrderVO>> getOrders() {
        List<Order> orders = orderMapper.selectList(null);
        if (orders.isEmpty()) {
            return CommonResponse.createForError("查无订单信息");
        }

        List<OrderVO> res = new ArrayList<>();
        for (Order order : orders) {
            OrderStatus orderStatus = orderStatusMapper.selectById(order.getOrderId());
            if (orderStatus == null) {
                return CommonResponse.createForError("查询出错：orderstatus无数据");
            }
            List<LineItemVO> lineItemVOList = getLineItems(order.getOrderId()).getData();
            OrderVO orderVO = orderToOrderVO(order, orderStatus, lineItemVOList);
            res.add(orderVO);
        }
        return CommonResponse.createForSuccess(res);
    }
    @Override
    public CommonResponse<List<SimpleOrderVO>> getSimpleOrders() {
        List<Order> orders = orderMapper.selectList(null);
        if (orders.isEmpty()) {
            return CommonResponse.createForError("查无订单信息");
        }

        List<SimpleOrderVO> res = new ArrayList<>();
        for (Order order : orders) {
            OrderStatus orderStatus = orderStatusMapper.selectById(order.getOrderId());
            if (orderStatus == null) {
                return CommonResponse.createForError("查询出错：orderstatus无数据");
            }
            List<LineItemVO> lineItemVOList = getLineItems(order.getOrderId()).getData();
            SimpleOrderVO simpleOrderVO = orderToSimple(order, orderStatus, lineItemVOList);
            res.add(simpleOrderVO);
        }
        return CommonResponse.createForSuccess(res);
    }

    @Override
    public CommonResponse<List<OrderVO>> getOrdersByUsername(String username) {
        QueryWrapper<Order> orderQW = new QueryWrapper<>();
        orderQW.eq("userid", username);
        List<Order> orders = orderMapper.selectList(orderQW);
        if (orders.isEmpty()) {
            return CommonResponse.createForError("该用户没有订单信息");
        }

        List<OrderVO> res = new ArrayList<>();
        for (Order order : orders) {
            OrderStatus orderStatus = orderStatusMapper.selectById(order.getOrderId());
            if (orderStatus == null) {
                return CommonResponse.createForError("查询出错");
            }
            List<LineItemVO> lineItemVOList = getLineItems(order.getOrderId()).getData();
            OrderVO orderVO = orderToOrderVO(order, orderStatus, lineItemVOList);
            res.add(orderVO);
        }
        return CommonResponse.createForSuccess(res);
    }
    @Override
    public OrderVO getOrdersByUsernameList(String username) {
        QueryWrapper<Order> orderQW = new QueryWrapper<>();
        orderQW.eq("userid", username);
        List<Order> orders = orderMapper.selectList(orderQW);
        OrderStatus orderStatus = orderStatusMapper.selectById(orders.get(0).getOrderId());
        List<LineItemVO> lineItemVOList = getLineItems(orders.get(0).getOrderId()).getData();
        OrderVO orderVO = orderToOrderVO(orders.get(0), orderStatus, lineItemVOList);

        return orderVO;
    }
    @Override
    public CommonResponse<List<SimpleOrderVO>> getSimpleOrdersByUsername(String username) {
        QueryWrapper<Order> orderQW = new QueryWrapper<>();
        orderQW.eq("userid", username);
        List<Order> orders = orderMapper.selectList(orderQW);
        if (orders.isEmpty()) {
            return CommonResponse.createForError("该用户没有订单信息");
        }

        List<SimpleOrderVO> res = new ArrayList<>();
        for (Order order : orders) {
            OrderStatus orderStatus = orderStatusMapper.selectById(order.getOrderId());
            if (orderStatus == null) {
                return CommonResponse.createForError("查询出错：orderstatus无数据");
            }
            List<LineItemVO> lineItemVOList = getLineItems(order.getOrderId()).getData();
            SimpleOrderVO simpleOrderVO = orderToSimple(order, orderStatus, lineItemVOList);
            res.add(simpleOrderVO);
        }
        return CommonResponse.createForSuccess(res);
    }

    @Override
    public CommonResponse<OrderVO> initOrder(CartVO cart, OrderVO orderVO) {
        if (!cart.isMerged()) return CommonResponse.createForError("未登录");
        if (cart.getItemMap().isEmpty()) return CommonResponse.createForError("购物车为空");
        // 初始化订单的userid
        orderVO.setUsername(cart.getUsername());
        // 初始化订单id
        int orderId = this.getNextId("ordernum"), lineId = 1;
        orderVO.setOrderId(orderId);
        // 将所有购物车内的商品做成LineItem添加进入订单orderVO，初始化订单内容
        for (CartItemVO cartItemVO : cart.getItemMap().values()) {
            LineItemVO lineItemVO = this.cartItemToLineItem(cartItemVO);
            lineItemVO.setOrderId(orderId);
            lineItemVO.setLineNumber(lineId);
            orderVO.getLineItems().add(lineItemVO);
            ++lineId;
        }

        orderVO.setOrderDate(new Date());
        orderVO.setStatus("N");


        try {
            Map<String, Object> orderMap = orderVOtoPO(orderVO);
            Order order = (Order) orderMap.get("order");
            OrderStatus orderStatus = (OrderStatus) orderMap.get("status");
            orderMapper.insert(order);
            orderStatusMapper.insert(orderStatus);
            for (LineItemVO lineItemVO : orderVO.getLineItems()) {
                LineItem lineItem = this.lineItemVOtoPO(lineItemVO);
                lineItemMapper.insert(lineItem);
            }
            return CommonResponse.createForSuccess(orderVO);
        } catch (Exception e) {
            log.error("订单持久化过程错误", e);
            return CommonResponse.createForError("订单持久化过程错误");
        }
    }

    @Override
    public CommonResponse payOrder(int orderId) {
        try {
            UpdateWrapper<OrderStatus> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("orderid", orderId).set("status", "P");
            orderStatusMapper.update(null, updateWrapper);
            return CommonResponse.createForSuccessMessage("订单状态更新成功");
        } catch (Exception e){
            log.error("订单状态更新过程错误", e);
            return CommonResponse.createForError("订单状态更新过程错误");
        }
    }

    @Override
    public CommonResponse<List<LineItemVO>> getLineItems(int orderId) {
        // 构造条件查询器
        QueryWrapper<LineItem> queryWrapper = new QueryWrapper<>();
        // 设置查询条件
        queryWrapper.eq("orderid", orderId);
        List<LineItem> lineItems = lineItemMapper.selectList(queryWrapper);

        List<LineItemVO> res = new ArrayList<>();
        for (LineItem lineItem : lineItems) {
            LineItemVO lineItemVO = lineItemToVO(lineItem);
            res.add(lineItemVO);
        }

        return CommonResponse.createForSuccess(res);
    }

    private LineItemVO lineItemToVO(LineItem lineItem) {

        LineItemVO lineItemVO = new LineItemVO();
        lineItemVO.setOrderId(lineItem.getOrderId());
        lineItemVO.setLineNumber(lineItem.getLinenum());
        lineItemVO.setItemId(lineItem.getItemId());
        lineItemVO.setQuantity(lineItem.getQuantity());
        lineItemVO.setUnitPrice(lineItem.getUnitPrice());

//        lineItemVO.setItem(itemVO);
        lineItemVO.calculateTotal();
        return lineItemVO;
    }

    private LineItem lineItemVOtoPO(LineItemVO lineItemVO){
        LineItem lineItem = new LineItem();
        lineItem.setOrderId(lineItemVO.getOrderId());
        lineItem.setLinenum(lineItemVO.getLineNumber());
        lineItem.setItemId(lineItemVO.getItemId());
        lineItem.setQuantity(lineItemVO.getQuantity());
        lineItem.setUnitPrice(lineItemVO.getUnitPrice());
        return lineItem;
    }

    public OrderVO orderToOrderVO(Order order, OrderStatus orderStatus, List<LineItemVO> lineItems) {
        OrderVO orderVO = new OrderVO();
        // order 注入
        orderVO.setOrderId(order.getOrderId());
        orderVO.setUsername(order.getUsername());
        orderVO.setOrderDate(order.getOrderDate());
        orderVO.setShipAddress1(order.getShipAddress1());
        orderVO.setShipAddress2(order.getShipAddress2());
        orderVO.setShipCity(order.getShipCity());
        orderVO.setShipState(order.getShipState());
        orderVO.setShipZip(order.getShipZip());
        orderVO.setShipCountry(order.getShipCountry());
        orderVO.setBillAddress1(order.getBillAddress1());
        orderVO.setBillAddress2(order.getBillAddress2());
        orderVO.setBillCity(order.getBillCity());
        orderVO.setBillState(order.getBillState());
        orderVO.setBillZip(order.getBillZip());
        orderVO.setBillCountry(order.getBillCountry());
        orderVO.setCourier(order.getCourier());
        orderVO.setTotalPrice(order.getTotalPrice());
        orderVO.setBillToFirstName(order.getBillToFirstName());
        orderVO.setBillToLastName(order.getBillToLastName());
        orderVO.setShipToFirstName(order.getShipToFirstName());
        orderVO.setShipToLastName(order.getShipToLastName());
        orderVO.setCreditCard(order.getCreditCard());
        orderVO.setExpiryDate(order.getExpiryDate());
        orderVO.setCardType(order.getCardType());
        orderVO.setLocale(order.getLocale());

        // orderstaus 注入
        orderVO.setStatus(orderStatus.getStatus());
        // LineItems
        orderVO.setLineItems(lineItems);

        return orderVO;
    }

    private SimpleOrderVO orderToSimple(Order order, OrderStatus orderStatus, List<LineItemVO> lineItems){
        SimpleOrderVO simpleOrderVO = new SimpleOrderVO();
        simpleOrderVO.setOrderId(order.getOrderId());
        simpleOrderVO.setUsername(order.getUsername());
        simpleOrderVO.setOrderDate(order.getOrderDate());
        simpleOrderVO.setStatus(orderStatus.getStatus());

        BigDecimal total = new BigDecimal(0);
        for (LineItemVO lineItemVO : lineItems) {
            total = total.add(lineItemVO.getTotal());
        }

        simpleOrderVO.setTotalPrice(total);
        return simpleOrderVO;
    }

    private LineItemVO cartItemToLineItem(CartItemVO cartItemVO){
        LineItemVO lineItemVO = new LineItemVO();
        lineItemVO.setItemId(cartItemVO.getItemId());
        lineItemVO.setQuantity(cartItemVO.getQuantity());
        lineItemVO.setUnitPrice(cartItemVO.getUnitPrice());
        lineItemVO.setTotal(cartItemVO.getTotal());
        return lineItemVO;
    }

    private Map<String, Object> orderVOtoPO(OrderVO orderVO){
        Order order = new Order();
        order.setOrderId(orderVO.getOrderId());
        order.setUsername(orderVO.getUsername());
        order.setOrderDate(orderVO.getOrderDate());
        order.setShipAddress1(orderVO.getShipAddress1());
        order.setShipAddress2(orderVO.getShipAddress2());
        order.setShipCity(orderVO.getShipCity());
        order.setShipState(orderVO.getShipState());
        order.setShipZip(orderVO.getShipZip());
        order.setShipCountry(orderVO.getShipCountry());
        order.setBillAddress1(orderVO.getBillAddress1());
        order.setBillAddress2(orderVO.getBillAddress2());
        order.setBillCity(orderVO.getBillCity());
        order.setBillState(orderVO.getBillState());
        order.setBillZip(orderVO.getBillZip());
        order.setBillCountry(orderVO.getBillCountry());
        order.setCourier(orderVO.getCourier());
        order.setTotalPrice(orderVO.getTotalPrice());
        order.setBillToFirstName(orderVO.getBillToFirstName());
        order.setBillToLastName(orderVO.getBillToLastName());
        order.setShipToFirstName(orderVO.getShipToFirstName());
        order.setShipToLastName(orderVO.getShipToLastName());
        order.setCreditCard(orderVO.getCreditCard());
        order.setExpiryDate(orderVO.getExpiryDate());
        order.setCardType(orderVO.getCardType());
        order.setLocale(orderVO.getLocale());

        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderVO.getOrderId());
        // 没用的linenum字段暂时用orderid代替
        orderStatus.setLineNum(orderVO.getOrderId());
        orderStatus.setTimestamp(orderVO.getOrderDate());
        orderStatus.setStatus(orderVO.getStatus());

        Map<String, Object> res = Collections.synchronizedMap(new HashMap<>());
        res.put("order", order);
        res.put("status", orderStatus);
        return res;
    }

    public int getNextId(String name) {
        Sequence sequence = sequenceMapper.selectById(name);
        if (sequence == null) {
            throw new RuntimeException("Error: A null sequence was returned from the database (could not get next "
                    + name + " sequence).");
        }
        sequence.setNextId(sequence.getNextId() + 1);
        sequenceMapper.updateById(sequence);
        return sequence.getNextId();
    }
//    @Override
//    public CommonResponse payOrder(int orderId) {
//        try {
//            UpdateWrapper<OrderStatus> updateWrapper = new UpdateWrapper<>();
//            updateWrapper.eq("orderid", orderId).set("status", "P");
//            orderStatusMapper.update(null, updateWrapper);
//            return CommonResponse.createForSuccessMessage("订单状态更新成功");
//        } catch (Exception e){
//            log.error("订单状态更新过程错误", e);
//            return CommonResponse.createForError("订单状态更新过程错误");
//        }
//    }
//
//    @Override
//    public CommonResponse<List<LineItemVO>> getLineItems(int orderId) {
//        // 构造条件查询器
//        QueryWrapper<LineItem> queryWrapper = new QueryWrapper<>();
//        // 设置查询条件
//        queryWrapper.eq("orderid", orderId);
//        List<LineItem> lineItems = lineItemMapper.selectList(queryWrapper);
//
//        List<LineItemVO> res = new ArrayList<>();
//        for (LineItem lineItem : lineItems) {
//            LineItemVO lineItemVO = lineItemToVO(lineItem);
//            res.add(lineItemVO);
//        }
//
//        return CommonResponse.createForSuccess(res);
//    }
//
//    private LineItemVO lineItemToVO(LineItem lineItem) {
//
//        LineItemVO lineItemVO = new LineItemVO();
//        lineItemVO.setOrderId(lineItem.getOrderId());
//        lineItemVO.setLineNumber(lineItem.getLineNum());
//        lineItemVO.setItemId(lineItem.getItemId());
//        lineItemVO.setQuantity(lineItem.getQuantity());
//        lineItemVO.setUnitPrice(lineItem.getUnitPrice());
//        lineItemVO.setItem(itemVO);
//        lineItemVO.calculateTotal();
//        return lineItemVO;
//    }
//
//    private LineItem lineItemVOtoPO(LineItemVO lineItemVO){
//        LineItem lineItem = new LineItem();
//        lineItem.setOrderId(lineItemVO.getOrderId());
//        lineItem.setLineNum(lineItemVO.getLineNumber());
//        lineItem.setItemId(lineItemVO.getItemId());
//        lineItem.setQuantity(lineItemVO.getQuantity());
//        lineItem.setUnitPrice(lineItemVO.getUnitPrice());
//        return lineItem;
//    }
//
//    private OrderVO orderToOrderVO(Order order, OrderStatus orderStatus, List<LineItemVO> lineItems) {
//        OrderVO orderVO = new OrderVO();
//        orderVO.setOrderId(order.getOrderId());
//        orderVO.setUsername(order.getUsername());
//        orderVO.setOrderDate(order.getOrderDate());
//        orderVO.setShipAddress1(order.getShipAddress1());
//        orderVO.setShipAddress2(order.getShipAddress2());
//        orderVO.setShipCity(order.getShipCity());
//        orderVO.setShipState(order.getShipState());
//        orderVO.setShipZip(order.getShipZip());
//        orderVO.setShipCountry(order.getShipCountry());
//        orderVO.setBillAddress1(order.getBillAddress1());
//        orderVO.setBillAddress2(order.getBillAddress2());
//        orderVO.setBillCity(order.getBillCity());
//        orderVO.setBillState(order.getBillState());
//        orderVO.setBillZip(order.getBillZip());
//        orderVO.setBillCountry(order.getBillCountry());
//        orderVO.setCourier(order.getCourier());
//        orderVO.setTotalPrice(order.getTotalPrice());
//        orderVO.setBillToFirstName(order.getBillToFirstName());
//        orderVO.setBillToLastName(order.getBillToLastName());
//        orderVO.setShipToFirstName(order.getShipToFirstName());
//        orderVO.setShipToLastName(order.getShipToLastName());
//        orderVO.setCreditCard(order.getCreditCard());
//        orderVO.setExpiryDate(order.getExpiryDate());
//        orderVO.setCardType(order.getCardType());
//        orderVO.setLocale(order.getLocale());
//
//
//        orderVO.setStatus(orderStatus.getStatus());
//        // LineItems
//        orderVO.setLineItems(lineItems);
//
//        return orderVO;
//    }
//
//    private SimpleOrderVO orderToSimple(Order order, OrderStatus orderStatus, List<LineItemVO> lineItems){
//        SimpleOrderVO simpleOrderVO = new SimpleOrderVO();
//        simpleOrderVO.setOrderId(order.getOrderId());
//        simpleOrderVO.setUsername(order.getUsername());
//        simpleOrderVO.setOrderDate(order.getOrderDate());
//        simpleOrderVO.setStatus(orderStatus.getStatus());
//
//        BigDecimal total = new BigDecimal(0);
//        for (LineItemVO lineItemVO : lineItems) {
//            total = total.add(lineItemVO.getTotal());
//        }
//
//        simpleOrderVO.setTotalPrice(total);
//        return simpleOrderVO;
//    }
//
//    private LineItemVO cartItemToLineItem(CartItemVO cartItemVO){
//        LineItemVO lineItemVO = new LineItemVO();
//        lineItemVO.setItemId(cartItemVO.getItemId());
//        lineItemVO.setQuantity(cartItemVO.getQuantity());
//        lineItemVO.setUnitPrice(cartItemVO.getUnitPrice());
//        lineItemVO.setTotal(cartItemVO.getTotal());
//        return lineItemVO;
//    }
//
//    private Map<String, Object> orderVOtoPO(OrderVO orderVO){
//        Order order = new Order();
//        order.setOrderId(orderVO.getOrderId());
//        order.setUsername(orderVO.getUsername());
//        order.setOrderDate(orderVO.getOrderDate());
//        order.setShipAddress1(orderVO.getShipAddress1());
//        order.setShipAddress2(orderVO.getShipAddress2());
//        order.setShipCity(orderVO.getShipCity());
//        order.setShipState(orderVO.getShipState());
//        order.setShipZip(orderVO.getShipZip());
//        order.setShipCountry(orderVO.getShipCountry());
//        order.setBillAddress1(orderVO.getBillAddress1());
//        order.setBillAddress2(orderVO.getBillAddress2());
//        order.setBillCity(orderVO.getBillCity());
//        order.setBillState(orderVO.getBillState());
//        order.setBillZip(orderVO.getBillZip());
//        order.setBillCountry(orderVO.getBillCountry());
//        order.setCourier(orderVO.getCourier());
//        order.setTotalPrice(orderVO.getTotalPrice());
//        order.setBillToFirstName(orderVO.getBillToFirstName());
//        order.setBillToLastName(orderVO.getBillToLastName());
//        order.setShipToFirstName(orderVO.getShipToFirstName());
//        order.setShipToLastName(orderVO.getShipToLastName());
//        order.setCreditCard(orderVO.getCreditCard());
//        order.setExpiryDate(orderVO.getExpiryDate());
//        order.setCardType(orderVO.getCardType());
//        order.setLocale(orderVO.getLocale());
//
//        OrderStatus orderStatus = new OrderStatus();
//        orderStatus.setOrderId(orderVO.getOrderId());
//        // 没用的linenum字段暂时用orderid代替
//        orderStatus.setLineNum(orderVO.getOrderId());
//        orderStatus.setTimestamp(orderVO.getOrderDate());
//        orderStatus.setStatus(orderVO.getStatus());
//
//        Map<String, Object> res = Collections.synchronizedMap(new HashMap<>());
//        res.put("order", order);
//        res.put("status", orderStatus);
//        return res;
//    }
//
//    private int getNextId(String name) {
//        Sequence sequence = sequenceMapper.selectById(name);
//        if (sequence == null) {
//            throw new RuntimeException("Error: A null sequence was returned from the database (could not get next "
//                    + name + " sequence).");
//        }
//        sequence.setNextId(sequence.getNextId() + 1);
//        sequenceMapper.updateById(sequence);
//        return sequence.getNextId();
//    }
}
