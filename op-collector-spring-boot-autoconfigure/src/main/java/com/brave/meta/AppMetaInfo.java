package com.brave.meta;

import lombok.Data;

/**
 * @Author: junzhang
 * @Description:
 * @Date: 2020/1/6 11:45 PM
 **/
@Data
public class AppMetaInfo {
	String applicationName;
	String ip;
	Integer port;
	String serviceName;
	String callUrl;
}
