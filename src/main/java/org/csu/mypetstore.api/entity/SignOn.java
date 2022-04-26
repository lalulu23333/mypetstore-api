package org.csu.mypetstore.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
//import org.csu.mypetstore.api.utils.encrypt.annotation.SensitiveData;
//import org.csu.mypetstore.api.utils.encrypt.annotation.SensitiveField;

@Data
//@SensitiveData
@TableName(value = "signon")
public class SignOn {

    @TableId(value = "username", type = IdType.INPUT)
    private String username;
    private String password;
}
