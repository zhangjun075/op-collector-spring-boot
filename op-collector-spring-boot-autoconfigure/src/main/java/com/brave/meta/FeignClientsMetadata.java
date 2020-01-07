package com.brave.meta;

import java.util.List;

import lombok.Data;

/**
 * @Author: junzhang
 * @Description:
 * @Date: 2020/1/7 7:49 PM
 **/
@Data
public class FeignClientsMetadata {
	List<ServiceMetaInfo> serviceMetadatas;
}
