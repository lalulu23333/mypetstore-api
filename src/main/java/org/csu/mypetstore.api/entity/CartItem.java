package org.csu.mypetstore.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("cartitem")
public class CartItem {

    @TableId(value = "cartitemid", type = IdType.AUTO)
    private Integer cartItemId;
    @TableField(value = "userid")
    private String username;
    @TableField(value = "itemid")
    private String itemId;
    private int quantity;
    @TableField(value = "unitprice")
    private BigDecimal unitPrice;
    @TableField(value = "instock")
    private boolean inStock; // 这个字段存在的必要性待定
    // 使用一些数据冗余减少代码量
    @TableField(value = "attr1")
    private String attribute1;
    @TableField(value = "attr2")
    private String attribute2;
    @TableField(value = "inventory")
    private long InventQty;
}
