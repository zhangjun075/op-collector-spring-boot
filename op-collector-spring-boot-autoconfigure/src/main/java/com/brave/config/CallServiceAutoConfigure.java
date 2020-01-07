package com.brave.config;

import com.brave.interceptor.BraveHttpInterceptor;
import com.brave.meta.FeignClientsMetadata;
import com.brave.processor.FeignClientProcessor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: junzhang
 * @Description:
 * @Date: 2020/1/6 11:52 PM
 **/
@Configuration
@Slf4j
public class CallServiceAutoConfigure {

	@Bean
	@ConditionalOnClass(name = "org.springframework.cloud.netflix.feign.FeignClient")
	FeignClientProcessor feignClientInterceptor() {
		return new FeignClientProcessor();
	}

	@Bean
	@ConditionalOnClass(name = "org.springframework.cloud.netflix.feign.FeignClient")
	@ConditionalOnBean(FeignClientProcessor.class)
	FeignClientsMetadata clientsMetadata(FeignClientProcessor clientProcessor) {
		FeignClientsMetadata clientsMetadata = new FeignClientsMetadata();
		clientsMetadata.setServiceMetadatas(clientProcessor.getMetadatas());
		return clientsMetadata;
	}

	/**
	 * restTemplate.addInterceptors(Collections.singletonList(new BraveHttpInterceptor()))
	 **/
	@Bean
	@ConditionalOnClass(name = "org.springframework.web.client.RestTemplate")
	BraveHttpInterceptor httpInterceptor() {
		return new BraveHttpInterceptor();
	}


}
