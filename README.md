# brave-collector-starter
## 代码地址：[系统调用关系采集器(轻量版)&系统基本信息采集器]()
## 背景
现在部门基本所有服务都通过restful api的方式暴露出来。当然也有通过dubbo方式的，但是已经是极为少数了。服务化采用spring cloud生态完成。对于服务的调用，一般有如下两种方式：1、springcloud feignclient的方式；2、RestTemplate方式。
服务之间调用很多是违背好莱坞准则的。为了梳理系统调用关系，且采集系统相关信息，且目前也在做系统的分级，特开发此starter，尽量做到对系统低侵入。由于采用sdk的方式，因此，灵活性较好，便于后续进行其他方面的扩展。
另外阐述下本人使用框架的准则：使用一款中间件，你一定要能hold的住他，对他足够的了解，否则等于给自己买了一颗雷。

## 功能
* 采集系统信息，包括应用名称、应用端口、应用IP
* 采集系统调用关系，包括FeignClient的调用，RestTemplate的调用，据此可以画出完整的调用链路。
* 异步发送的集成。//todo
* 通过增加

## 特点
* FeignClient的采集无侵入，对于RestTemplate，需要添加一个Interceptor，即可。
* 对于FeignClient的信息，由于是容器启动的时候初始化，因此在容器启动阶段采集，不影响运行时性能。
* 对于RestTemplate的请求，在运行时拦截，可能会影响，
* 采用了springboot的自动装配机制，对于本sdk中部分代码的加载，都依赖于项目中是否用了那两种调用方式。

## 使用
* 打包，直接引用starter
* 本代码可以直接扩展，添加异步发送的方式，存储自定义。//todo
* 对于Application的信息，如果想要使用的话，直接注入@Autowired AppMetaInfo 即可。对于FeignClient的调用信息获取，直接自动化注入@Autowired FeignClientsMetadata即可。
对于RestTemplate的调用，添加如下:
```java
restTemplate.addInterceptors(Collections.singletonList(new BraveHttpInterceptor()));
```
Interceptor中会拦截RestTemplate请求，接下来会采集请求信息。

## 代码分析
* 对于Application的信息采集，比较简单,主要是取项目中的yml文件配置信息，采用@Value方式即可，对于ip和端口，采用IpTool封装的方法即可，参见源码。
```java
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
```

* 采集FeignClient的请求信息
	* 由于FeignClient实例化采用的是jdk 动态代理的方式，容器中存在的是代理类。取接口的注解不现实。
	* FeignClient的原理，参见我之前的文章[FeignClient源码解析](https://zhangjun075.github.io/passages/springcloud-feign/)
	* 本方案的思路，采用BearFactoryAware钩子回调的方式，从BeanFactory中获取代理对象实例。大家看过我之前的文章，就知道，所有的代理类，都是通过FeignClientFactoryBean的getObject()方法来获取的。
	这里，直接采用如下方式，即可：
	
	```java
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		String[] beanDefinitionNames = ((DefaultListableBeanFactory) beanFactory).getBeanDefinitionNames();
		Arrays.stream(beanDefinitionNames).forEach(
				definitionName -> {
					BeanDefinition beanDefinition = ((DefaultListableBeanFactory) beanFactory).getBeanDefinition(definitionName);
					String feignClientProxyName = beanDefinition.getBeanClassName();
					log.info("test feignClientProxyName is :{}",feignClientProxyName);
					if(!Objects.isNull(feignClientProxyName) && CollectorInstant.FEIGN_CLIENT_FACTORY_BEAN.equals(feignClientProxyName)) {
						MutablePropertyValues propValues = beanDefinition.getPropertyValues();
						if(null != propValues) {
							List<PropertyValue> pvs = propValues.getPropertyValueList();
							pvs.forEach(propertyValue -> {
								ServiceMetaInfo metaInfo = new ServiceMetaInfo();
								if(propertyValue.getName().equals(CollectorInstant.FEIGN_CLIENT_SERVICE_URL)) {
									metaInfo.setCallUrl(propertyValue.getValue().toString());
								}
								if(propertyValue.getName().equals(CollectorInstant.FEIGN_CLIENT_SERVICE_NAME)) {
									metaInfo.setServiceName(propertyValue.getValue().toString());
								}
								metadatas.add(metaInfo);
								log.info("feignClient property is :{}={}",propertyValue,propertyValue.getValue());
							});

						}
					}
				}
		);
	}
	```

	* 代理类的请求信息，都存在MutablePropertyValues对象中，因此，获取该对象，取出MutablePropertyValues即可获取FeignClient的配置信息。
日志打印如下：
	
```java
2020-01-07 17:31:31.947  INFO 59637 --- [           main] com.brave.test.BeanFactoryAwareTest      : feignClient property is :bean property 'url'=
2020-01-07 17:31:31.947  INFO 59637 --- [           main] com.brave.test.BeanFactoryAwareTest      : feignClient property is :bean property 'path'=
2020-01-07 17:31:31.947  INFO 59637 --- [           main] com.brave.test.BeanFactoryAwareTest      : feignClient property is :bean property 'name'=eureka-client2
2020-01-07 17:31:31.947  INFO 59637 --- [           main] com.brave.test.BeanFactoryAwareTest      : feignClient property is :bean property 'type'=com.brave.client.DemoClient
2020-01-07 17:31:31.947  INFO 59637 --- [           main] com.brave.test.BeanFactoryAwareTest      : feignClient property is :bean property 'decode404'=false
2020-01-07 17:31:31.947  INFO 59637 --- [           main] com.brave.test.BeanFactoryAwareTest      : feignClient property is :bean property 'fallback'=void
2020-01-07 17:31:31.947  INFO 59637 --- [           main] com.brave.test.BeanFactoryAwareTest      : feignClient property is :bean property 'fallbackFactory'=void
```

* RestTemplate请求信息采集，略微有侵入，采用了Spring的ClientHttpRequestInterceptor这个拦截器。大家网上搜索下即可。
* 自动装配机制的使用，参照了SpringBoot启动容器的方式，对于Conditional的理解，大家多学习。
```java
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
```
## 总结语
还有一些待完善的扩展点，明天继续，晚安。


