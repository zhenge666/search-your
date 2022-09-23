package com.your.business.search.vo;

import lombok.Data;

/**
 * 分页响应对象
 *
 * @author zhangzhen
 * @Date 2021/9/15 下午9:36
 */
@Data
public class PageVO<T> extends ListVO<T> {

    /**
     * 总记录数
     */
    private long total;

}
