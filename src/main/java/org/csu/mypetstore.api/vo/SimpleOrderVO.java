package org.csu.mypetstore.api.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class SimpleOrderVO {
    // orders 表字段注入
    private int orderId;
    private String username;
    private Date orderDate;

    private String status;

    // 计算得出
    private BigDecimal totalPrice;
}
