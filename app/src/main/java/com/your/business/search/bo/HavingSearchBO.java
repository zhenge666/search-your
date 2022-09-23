package com.your.business.search.bo;

import com.your.base.model.BaseBo;
import com.your.search.center.facade.annotation.EsQuery;
import com.your.search.center.facade.annotation.EsStats;
import com.your.search.center.facade.enums.QueryTypeEnum;
import com.your.search.center.facade.enums.StatsTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 搜索数量查询
 *
 * @author 666
 * @date 2022-04-06 17:10:16
 */
@Data
public class HavingSearchBO extends BaseBo {

    /**
     * 用户id
     */
    @EsQuery(name = "log.userId")
    @EsStats(type = StatsTypeEnum.Having, fieldName = "log.userId", startField = "startCount", endField = "endCount")
    private String userNo;

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

    /**
     * 数量范围
     */
    private Long startCount;

    /**
     * 数量范围
     */
    private Long endCount;

}

