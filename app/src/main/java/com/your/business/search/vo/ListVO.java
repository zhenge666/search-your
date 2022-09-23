package com.your.business.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 列表响应对象
 *
 * @author zhangzhen
 * @Date 2021/10/18 下午5:36
 */
@Data
public class ListVO<T> extends BaseVO {

    /**
     * 列表数据
     */
    private List<T> records;

}
