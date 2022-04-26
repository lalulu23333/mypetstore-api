package org.csu.mypetstore.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "log")
public class Log {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer logId;
    @TableField(value = "logUserId")
    private String logUserId;
    @TableField(value = "logInfo")
    private String logInfo;
}
