package com.brave.config;

import com.brave.meta.AppMetaInfo;
import com.brave.util.IpTool;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * @Author: junzhang
 * @Description:
 * 	initialize the app info:AppMetaInfo
 * @Date: 2020/1/6 11:22 PM
 **/
@Configuration
@Slf4j
public class ApplicationAutoConfigure {

	@Value("${spring.application.name}")
	String appName;
	@Value("${server.port}")
	Integer port;

	@Bean
	AppMetaInfo appMetaInfo() {
		AppMetaInfo appMetaInfo = new AppMetaInfo();
		appMetaInfo.setApplicationName(appName);
		appMetaInfo.setPort(port);
		String ip = IpTool.getLocalIP();
		appMetaInfo.setIp(ip);
		log.info("App info is {}",appMetaInfo);
		return new AppMetaInfo();
	}
}
