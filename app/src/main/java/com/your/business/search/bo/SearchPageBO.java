package com.your.business.search.bo;

import com.your.search.center.facade.annotation.EsQuery;
import com.your.search.center.facade.enums.QueryTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 查询记录分页 -> /admin/search/page
 *
 * @author 666
 * @date 2022-04-06 17:10:16
 */
@Data
public class SearchPageBO extends PageBO {

    /**
     * 业务类型，11:logo，取标签库：product_type
     */
    @EsQuery(name = "log.productType")
    private Integer productType;

    /**
     * 用户编号
     */
    @EsQuery(name = "log.userId")
    private Long userNo;

    /**
     * 开始时间
     */
    @EsQuery(name = "log.createTime", queryType = QueryTypeEnum.RANGE_GE)
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @EsQuery(name = "log.createTime", queryType = QueryTypeEnum.RANGE_LE)
    private LocalDateTime endTime;

}

