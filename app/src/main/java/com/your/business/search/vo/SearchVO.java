package com.your.business.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 查询对象
 *
 * @author zhangzhen
 * @Date 2022/4/6 下午5:34
 */
@Data
public class SearchVO extends BaseVO {

    /**
     * 搜索平台
     */
    private String source;

    /**
     * 搜索渠道
     */
    private String channel;

    /**
     * 品牌名
     */
    private String title;

    /**
     * slogan
     */
    private String subtitle;

    /**
     * 行业
     */
    private String industry;

    /**
     * 自定义描述
     */
    private String desc;

    /**
     * 核心词
     */
    private List<String> coreWords;

    /**
     * 扩展词
     */
    private List<String> extendWords;

    /**
     * 浏览页数
     */
    private Integer page;

}
