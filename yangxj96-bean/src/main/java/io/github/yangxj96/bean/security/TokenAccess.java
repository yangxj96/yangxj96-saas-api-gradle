/*****************************
 * Copyright (c) 2021 - 2023
 * author:yangxj96
 * email :yangxj96@gmail.com
 * date  :2023-01-07 00:07:12
 * Copyright (c) 2021 - 2023
 ****************************/

package io.github.yangxj96.bean.security;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.yangxj96.common.base.BasicEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * 存储的access token实体
 *
 * @author yangxj96
 * @version 1.0
 * @date 2023-01-07 00:14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "db_user.t_token_access")
public class TokenAccess extends BasicEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * token
     */
    @TableField(value = "token")
    private String token;

    /**
     * 所属用户id
     */
    @TableField(value = "username")
    private String username;

    /**
     * 权限对象
     */
    @TableField(value = "authentication")
    private byte[] authentication;

    /**
     * 到期时间
     */
    @TableField(value = "expiration_time")
    private LocalDateTime expirationTime;

}
