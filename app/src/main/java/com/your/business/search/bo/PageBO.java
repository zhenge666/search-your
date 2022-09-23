package com.your.business.search.bo;

import com.your.base.model.BaseBo;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 分页请求对象基础参数
 *
 * @author zhangzhen
 * @Date 2021/9/15 下午9:13
 */
@Data
public class PageBO extends BaseBo<Object> {

    /**
     * 当前页
     */
    @NotNull(message = "分页参数不可为空")
    @Min(value = 1, message = "分页参数错误")
    private Long current;

    /**
     * 每页大小
     */
    @NotNull(message = "分页参数不可为空")
    @Min(value = 1, message = "分页参数错误")
    @Max(value = 20, message = "分页参数错误")
    private Long size;

    /**
     * 用户身份类型
     */
    private Integer identityType;

}
