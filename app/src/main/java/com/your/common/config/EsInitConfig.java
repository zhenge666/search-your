package com.your.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * ES数据初始化动态配置
 *
 * @author zhangzhen
 * @Date 2022/4/11 下午4:06
 */
@Component
@RefreshScope
@ConfigurationProperties(prefix = "es.data")
@Data
public class EsInitConfig {

    /**
     * 是否开启初始化数据
     */
    private Boolean initOpen;

    /**
     * 数据起始行数
     */
    private Long fromCount;

    /**
     * 每次取数据的大小
     */
    private Long perSize;

}
