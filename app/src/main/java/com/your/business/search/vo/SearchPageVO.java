package com.your.business.search.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.your.common.annotation.EsResult;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 查询记录分页 -> /admin/search/page
 *
 * @author 666
 * @date 2022-04-06 17:10:16
 */
@Data
public class SearchPageVO extends SearchVO {

    /**
     * 查询id
     */
    private String version;

    /**
     * 用户id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    /**
     * 查询时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @EsResult(name = "log.createTime", sort = 1)
    private LocalDateTime timestamp;

}

