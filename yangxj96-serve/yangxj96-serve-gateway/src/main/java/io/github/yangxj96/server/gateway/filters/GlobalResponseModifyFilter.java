/*****************************
 * Copyright (c) 2021 - 2023
 * author:yangxj96
 * email :yangxj96@gmail.com
 * date  :2023-01-07 00:08:39
 * Copyright (c) 2021 - 2023
 ****************************/

package io.github.yangxj96.server.gateway.filters;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.yangxj96.common.respond.R;
import io.github.yangxj96.common.respond.RHttpHeadersExpand;
import io.github.yangxj96.common.respond.RStatus;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * ?????????????????????
 *
 * @author yangxj96
 * @version 1.0
 * @date 2023-01-07 00:14
 */
@Slf4j
@Component
public class GlobalResponseModifyFilter implements GlobalFilter, Ordered {

    @Resource
    private ServerCodecConfigurer serverCodecConfigurer;

    @Resource
    private ObjectMapper om;

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE + 1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("[???????????????-???????????????????????????]:???????????????");
        return chain.filter(exchange.mutate().response(decorate(exchange)).build());
    }

    private ServerHttpResponse decorate(ServerWebExchange exchange) {
        return new ModifyServerHttpResponse(exchange, serverCodecConfigurer, om);
    }

    public static class ModifyServerHttpResponse extends ServerHttpResponseDecorator {

        private final ServerCodecConfigurer serverCodecConfigurer;

        private final ServerWebExchange exchange;

        private final ObjectMapper om;

        public ModifyServerHttpResponse(ServerWebExchange exchange, ServerCodecConfigurer serverCodecConfigurer, ObjectMapper om) {
            super(exchange.getResponse());
            this.exchange = exchange;
            this.serverCodecConfigurer = serverCodecConfigurer;
            this.om = om;
        }

        @Override
        public @NotNull Mono<Void> writeWith(@NotNull Publisher<? extends DataBuffer> body) {
            HttpHeaders httpHeaders = new HttpHeaders();
            if (isNotModify(exchange, httpHeaders)) {
                return super.writeWith(body);
            }

            ClientResponse clientResponse = ClientResponse
                    .create(Objects.requireNonNull(getDelegate().getStatusCode()), serverCodecConfigurer.getReaders())
                    .headers(headers -> headers.putAll(httpHeaders))
                    .body(Flux.from(body))
                    .build();

            // ??????body
            Mono<String> modifiedBody = clientResponse.bodyToMono(String.class).map(s -> s);

            BodyInserter<Mono<String>, ReactiveHttpOutputMessage> bodyInserter =
                    BodyInserters.fromPublisher(modifiedBody, String.class);

            CachedBodyOutputMessage cachedBodyOutputMessage =
                    new CachedBodyOutputMessage(exchange, exchange.getResponse().getHeaders());

            return bodyInserter
                    .insert(cachedBodyOutputMessage, new BodyInserterContext())
                    .then(Mono.defer(() -> {
                        R result = new R();
                        HttpHeaders headers = exchange.getResponse().getHeaders();
                        String code = headers.getFirst(RHttpHeadersExpand.RESULT_CODE);
                        if (StringUtils.isNotEmpty(code)) {
                            result.setCode(Integer.parseInt(code));
                            result.setMsg(RStatus.getMsgByCode(result.getCode()));
                        } else {
                            result.setCode(0);
                            result.setMsg("success");
                        }
                        Flux<DataBuffer> messageBody = cachedBodyOutputMessage.getBody();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        Flux<DataBuffer> flux = messageBody
                                .map(buffer -> modify(buffer, result))
                                .switchIfEmpty(emptyBody(result))
                                .doOnNext(data -> {
                                    headers.setContentLength(data.readableByteCount());
                                    // ??????????????????code
                                    headers.remove(RHttpHeadersExpand.RESULT_CODE);
                                });

                        return getDelegate().writeWith(flux);
                    }));

        }

        //// ???????????????


        /**
         * ?????????????????????body,????????????
         *
         * @return {@link Boolean } ???|???
         */
        private boolean isNotModify(ServerWebExchange exchange, HttpHeaders headers) {
            String contentType = exchange.getAttribute(ServerWebExchangeUtils.ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR);
            if (contentType == null) {
                return false;
            }
            // ??????Content-Type?????????????????????
            headers.add(HttpHeaders.CONTENT_TYPE, contentType);
            // ?????????????????????
            return contentType.contains(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        }

        /**
         * body??????????????????????????????
         *
         * @param result ????????????
         * @return ????????????
         */
        private Flux<DataBuffer> emptyBody(R result) {
            try {
                return Flux.just(getDelegate().bufferFactory().wrap(om.writeValueAsBytes(result)));
            } catch (JsonProcessingException e) {
                return Flux.just(getDelegate().bufferFactory().wrap(formattingErr()));
            }
        }

        /**
         * ??????????????????
         *
         * @param buffer {@link DataBuffer}
         * @param result {@link DataBuffer} ????????????
         * @return {@link DataBuffer}
         */
        private DataBuffer modify(DataBuffer buffer, R result) {
            byte[] bytes;
            String str = String.valueOf(StandardCharsets.UTF_8.decode(buffer.toByteBuffer()));
            try {
                if (JSONUtil.isTypeJSON(str)) {
                    result.setData(om.readTree(str));
                } else {
                    result.setData(str);
                }
                bytes = om.writeValueAsBytes(result);
            } catch (JsonProcessingException e) {
                log.error("json????????????,????????????json???????????????,{}", e.getMessage());
                bytes = formattingErr();
            }
            DataBufferUtils.release(buffer);
            return getDelegate().bufferFactory().wrap(bytes);
        }

        private byte[] formattingErr() {
            return """
                    {"code": -1,"message": "???????????????"}
                    """.getBytes(StandardCharsets.UTF_8);
        }
    }

}
