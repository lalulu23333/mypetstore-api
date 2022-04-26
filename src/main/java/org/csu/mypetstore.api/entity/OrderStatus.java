package org.csu.mypetstore.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = "orderstatus")
public class OrderStatus {

    @TableId(value = "orderid", type = IdType.INPUT) // 可能为自增
//    @TableField(value = "orderid")
    private Integer orderId;
//    @TableId(value = "linenum", type = IdType.INPUT) // 可能为自增
    @TableField(value = "linenum")
    private int lineNum;
    private Date timestamp;
    private String status;
}
