package org.csu.mypetstore.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName(value = "lineitem")
public class LineItem {

    @TableId(value = "orderid", type = IdType.INPUT)
    private int orderId;
    @TableId()
    private int linenum;
    @TableField(value = "itemid")
    private String itemId;
    private int quantity;
    @TableField(value = "unitprice")
    private BigDecimal unitPrice; // 对应item表当中的listprice
}
