package com.brave.config;

import com.brave.processor.CollectInfoSender;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: junzhang
 * @Description:
 * @Date: 2020/1/21 3:45 PM
 **/
@Slf4j
@Configuration
public class MessageSenderConfigure {

	@Bean
	CollectInfoSender sender() {
		return new CollectInfoSender();
	}

}
