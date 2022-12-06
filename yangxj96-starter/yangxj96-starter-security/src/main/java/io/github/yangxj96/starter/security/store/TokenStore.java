package io.github.yangxj96.starter.security.store;

import io.github.yangxj96.bean.security.TokenAccess;
import io.github.yangxj96.bean.security.TokenRefresh;
import org.springframework.security.core.Authentication;

import java.sql.SQLException;

/**
 * token 存储方式
 */
public interface TokenStore {

    	/**
	 * 创建token<br/>
	 * 默认一小时有效期
	 *
	 * @param auth 认证信息
	 * @return 创建好的token
	 */
	TokenAccess create(Authentication auth) throws SQLException;

	/**
	 * 根据token读取Authentication信息
	 *
	 * @param token token
	 * @return {@link Authentication}
	 */
	Authentication read(String token);

	/**
	 * 删除token
	 *
	 * @param token token
	 * @return 是否删除成功
	 */
	boolean remove(String token);

	/**
	 * 刷新token
	 *
	 * @param refreshToken 刷新token
	 * @return 刷新后的token
	 */
	TokenRefresh refresh(String refreshToken);

	/**
	 * 用于定时任务自动清理过期token
	 */
	void autoClean() throws Exception;

}
