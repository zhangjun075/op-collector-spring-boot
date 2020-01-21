package com.brave.processor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.brave.meta.AppMetaInfo;
import com.brave.meta.FeignClientsMetadata;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author: junzhang
 * @Description:
 * @Date: 2020/1/21 1:39 PM
 **/
@Slf4j
public class CollectInfoSender implements InitializingBean {

	@Autowired
	AppMetaInfo appMetaInfo;

	@Autowired
	FeignClientsMetadata feignClientsMetadata;

	private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("collector-pool-%d").build();


	private static final ExecutorService executorService = new ThreadPoolExecutor(10,
			50,
			1,
			TimeUnit.MINUTES,
			new LinkedBlockingDeque<>(100),
			THREAD_FACTORY,
			new ThreadPoolExecutor.DiscardOldestPolicy());

	// 异步发送到后台
	public static <E> void send(E e) {
		log.info("collector send message is {}",e);
		CompletableFuture<String> future = CompletableFuture.supplyAsync(()->{
			// todo
			return "SUCCESS";
		},executorService).exceptionally(throwable -> {
			log.info("Collecter send message exception occurred: {}",throwable.getCause());
			return "FAIL";
		});

	}

	@Override
	public void afterPropertiesSet() {
		log.info("Collect App info is {}",appMetaInfo);
		log.info("Collect FeignClient info is {}",feignClientsMetadata);
		CompletableFuture<String> future = CompletableFuture.supplyAsync(()->{
			// todo
			return "SUCCESS";
		},executorService).exceptionally(throwable -> {
			log.info("Collecter send message exception occurred: {}",throwable.getCause());
			return "FAIL";
		});
	}
}
