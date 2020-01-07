package com.brave.interceptor;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * @Author: junzhang
 * @Description:
 * @Date: 2020/1/7 10:10 PM
 **/
@Slf4j
public class BraveHttpInterceptor implements ClientHttpRequestInterceptor {
	@Override
	public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes, ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
		log.info("request uri is :{}",httpRequest.getURI());
		return clientHttpRequestExecution.execute(httpRequest,bytes);
	}
}
