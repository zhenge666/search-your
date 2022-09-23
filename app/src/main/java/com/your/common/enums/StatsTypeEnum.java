package com.your.common.enums;

import lombok.Getter;

/**
 * ES聚合条件类型枚举
 *
 * @author zhangzhen
 * @Date 2022/4/6 下午5:46
 */
@Getter
public enum StatsTypeEnum {

    /**
     *
     */
    MAX("esMax", "最大值"),
    MIN("esMin", "最小值"),
    AVG("esAvg", "平均值"),
    SUM("esSum", "和"),
    COUNT("esCount", "数量"),
    STATS("esStats", "小于等于"),
    GROUP("esGroup", "分组"),
    Having("esGroupHaving", "分组后筛选数量"),
    ;

    private String code;
    private String name;

    StatsTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

}
