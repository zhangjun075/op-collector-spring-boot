package com.brave.processor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.brave.common.CollectorInstant;
import com.brave.meta.ServiceMetaInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * @Author: junzhang
 * @Description:
 * @Date: 2020/1/7 7:45 PM
 **/
@Slf4j
@Data
public class FeignClientProcessor implements BeanFactoryAware {

	private List<ServiceMetaInfo> metadatas;

	public FeignClientProcessor() {
		metadatas = Collections.emptyList();
	}

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
							ServiceMetaInfo metaInfo = new ServiceMetaInfo();
							pvs.forEach(propertyValue -> {
								if(propertyValue.getName().equals(CollectorInstant.FEIGN_CLIENT_SERVICE_URL)) {
									metaInfo.setFlag(true);
									metaInfo.setCallUrl(propertyValue.getValue().toString());
								}
								if(propertyValue.getName().equals(CollectorInstant.FEIGN_CLIENT_SERVICE_NAME)) {
									metaInfo.setFlag(true);
									metaInfo.setServiceName(propertyValue.getValue().toString());
								}
							});
							if (metaInfo.isFlag()) {
								metadatas.add(metaInfo);
								log.info("feignClient property is {}",metaInfo);
							}
						}
					}
				}
		);
	}
}
