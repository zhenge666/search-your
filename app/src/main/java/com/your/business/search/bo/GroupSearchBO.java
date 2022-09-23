package com.your.business.search.bo;

import com.your.base.model.BaseBo;
import com.your.search.center.facade.annotation.EsQuery;
import com.your.search.center.facade.annotation.EsStats;
import com.your.search.center.facade.enums.StatsTypeEnum;
import lombok.Data;

/**
 * 搜索分组查询
 *
 * @author 666
 * @date 2022-04-06 17:10:16
 */
@Data
public class GroupSearchBO extends BaseBo {

    /**
     * 用户id
     */
    @EsQuery(name = "log.userId")
    @EsStats(type = StatsTypeEnum.GROUP, fieldName = "log.productType")
    private Long userId;

}

