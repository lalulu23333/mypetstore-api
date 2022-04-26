package org.csu.mypetstore.api.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemVO {

    // item 表中的字段
    private String itemId;
    private String productId;
    private BigDecimal listPrice;
    private BigDecimal unitCost;
    private int supplierId;
    private String status;
    private String attribute1;
    private String attribute2;
    private String attribute3;
    private String attribute4;
    private String attribute5;

    // 附加的 item 所属 product 表字段
    private String categoryId;
    private String productName;
    private String productDescription;

    // 来自 inventory 表的库存
    private long quantity;
}
