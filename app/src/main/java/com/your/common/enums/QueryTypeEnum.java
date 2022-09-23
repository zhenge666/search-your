package com.your.common.enums;

import lombok.Getter;

/**
 * ES查询条件类型枚举
 *
 * @author zhangzhen
 * @Date 2022/4/6 下午5:46
 */
@Getter
public enum QueryTypeEnum {

    /**
     *
     */
    TERMS(1, "精确匹配"),
    MATCH(2, "分词匹配"),
    RANGE_GE(3, "大于等于"),
    RANGE_LE(4, "小于等于"),
    RANGE_GT(5, "大于"),
    RANGE_LT(6, "小于"),
    HAVE_OR_NO(7, "0表示没有:=0，1表示有:>0"),
    IDS(8, "多id查询"),
    TERMS_FILTER(9, "not in过滤"),
    ;

    private int code;
    private String name;

    QueryTypeEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public static QueryTypeEnum getByCode(int code) {
        QueryTypeEnum[] enums = QueryTypeEnum.values();
        for (QueryTypeEnum statusEnum : enums) {
            if (code == statusEnum.getCode()) {
                return statusEnum;
            }
        }
        return null;
    }

}
