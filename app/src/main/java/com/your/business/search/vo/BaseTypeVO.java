package com.your.business.search.vo;

import lombok.Data;

/**
 * 用于响应单一的基本类型结果
 *
 * @author zhangzhen
 * @date 2021-09-15 20:00:00
 */
@Data
public class BaseTypeVO<T> extends BaseVO {

    /**
     * 执行结果:基本类型(boolean int long  等)
     */
    private T result;

}
