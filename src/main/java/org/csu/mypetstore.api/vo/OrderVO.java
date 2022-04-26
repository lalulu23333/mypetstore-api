package org.csu.mypetstore.api.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class OrderVO {
    // orders 表字段注入
    private int orderId;
    private String username;
    private Date orderDate;
    private String shipAddress1;
    private String shipAddress2;
    private String shipCity;
    private String shipState;
    private String shipZip;
    private String shipCountry;
    private String billAddress1;
    private String billAddress2;
    private String billCity;
    private String billState;
    private String billZip;
    private String billCountry;
    private String courier;
    private BigDecimal totalPrice;
    private String billToFirstName;
    private String billToLastName;
    private String shipToFirstName;
    private String shipToLastName;
    private String creditCard;
    private String expiryDate;
    private String cardType;
    private String locale;

    // orderstatus 表字段注入
    private String status;

    // LineItemVO 注入
    private List<LineItemVO> lineItems = new ArrayList<>();
}
