package io.github.yangxj96.common.respond;

import org.springframework.http.HttpHeaders;

import java.io.Serial;

/**
 * 响应头类型扩展
 *
 * @author 杨新杰
 */
public class RHttpHeadersExpand extends HttpHeaders {

    @Serial
	private static final long serialVersionUID = -2727675696060704975L;

	/** 响应码 **/
	public static final String RESULT_CODE = "result-code";

}