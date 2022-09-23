package com.your.business.search.es.dto;

import com.your.business.search.entity.SearchLog;
import com.your.business.search.es.init.InitSearchLogCreator;
import com.your.common.annotation.EsStart;
import com.your.common.constant.EsConstant;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * ES-yanshi数据结构
 *
 * @author zhangzhen
 * @Date 2022/4/2 下午3:54
 */
@Data
@Document(indexName = EsConstant.SEARCH_LOG_NEW, shards = 2)
@EsStart(name = "查询", initClass = InitSearchLogCreator.class)
public class EsSearchLogDTO extends EsBaseDTO {

    /**
     * 查询版本号
     */
    @Id
    @Field(type = FieldType.Keyword)
    private String version;

    /**
     * 查询记录表
     */
    @Field(type = FieldType.Object)
    private SearchLog log;

}
