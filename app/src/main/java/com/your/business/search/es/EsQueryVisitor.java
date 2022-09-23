package com.your.business.search.es;

import com.alibaba.fastjson.JSON;
import com.your.base.exception.BizException;
import com.your.base.model.BaseBo;
import com.your.business.search.bo.PageBO;
import com.your.business.search.es.dto.EsBaseDTO;
import com.your.business.search.vo.BaseVO;
import com.your.common.util.EsBeanConvert;
import com.your.common.util.EsBeanUtil;
import com.your.search.center.facade.enums.StatsTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Max;
import org.elasticsearch.search.aggregations.metrics.Stats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ES读取通用类
 *
 * @author zhangzhen
 * @Date 2022/4/2 下午2:50
 */
@Service
@Slf4j
public class EsQueryVisitor {

    @Autowired
    private ElasticsearchRestTemplate template;

    @Autowired
    private EsOtherConfig otherConfig;

    /**
     * 根据文档id查询对象
     *
     * @param id
     * @param esClass
     * @param <T>
     * @return
     */
    public <T extends EsBaseDTO> T getByDocId(String id, Class<T> esClass) {
        T entity = template.get(id, esClass);
        return entity;
    }

    /**
     * 是否存在
     *
     * @param id
     * @param esClass
     * @param <T>
     * @return
     */
    public <T extends EsBaseDTO> boolean exists(String id, Class<T> esClass) {
        return template.exists(id, esClass);
    }

    /**
     * 根据文档id批量查询,全部字段返回
     *
     * @param ids
     * @param esClass
     * @param <T>
     * @return
     */
    public <T extends EsBaseDTO> List<T> getByDocIds(Set<String> ids, Class<T> esClass) {
        // 一次最多1000
        if (ids.size() >= otherConfig.getIdsLimit()) {
            throw new BizException(BizErrorEnum.ERROR_IDS_LIMIT);
        }
        NativeSearchQueryBuilder searchQuery = new NativeSearchQueryBuilder();
        searchQuery.withIds(ids);
        List<T> dtoList = template.multiGet(searchQuery.build(), esClass, template.getIndexCoordinatesFor(esClass));
        return EsBeanUtil.filterNullToList(dtoList);
    }

    /**
     * 根据文档id批量查询,指定返回字段
     *
     * @param ids
     * @param esClass
     * @param <T>
     * @return
     */
    public <T extends EsBaseDTO> List<T> getByDocIds(Set<String> ids, Class<T> esClass, Class voClass) {
        // 一次最多1000
        if (ids.size() >= otherConfig.getIdsLimit()) {
            throw new BizException(BizErrorEnum.ERROR_IDS_LIMIT);
        }
        NativeSearchQueryBuilder searchQuery = new NativeSearchQueryBuilder();
        searchQuery.withQuery(QueryBuilders.idsQuery().addIds(ids.toArray(new String[ids.size()])));
        List<String> includeList = EsBeanConvert.voFindField(voClass);
        if (CollectionUtils.isNotEmpty(includeList)) {
            searchQuery.withFields(includeList.toArray(new String[includeList.size()]));
        }
        SearchHits<T> hits = template.search(searchQuery.build(), esClass);
        List<T> dtoList = (List) SearchHitSupport.unwrapSearchHits(hits);
        return EsBeanUtil.filterNullToList(dtoList);
    }

    /**
     * 根据查询条件批量查询
     *
     * @param reqOrBo req请求参数或bo请求参数
     * @param esClass
     * @param <T>
     * @return
     */
    public <T extends EsBaseDTO, K extends BaseVO> List<T> getList(Object reqOrBo, Class<T> esClass, Class<K> voClass) {
        Query query = EsBeanConvert.convertQuery(reqOrBo, voClass);
        query.setTrackTotalHits(true);
        SearchHits<T> hits = template.search(query, esClass);
        List<T> dtoList = (List) SearchHitSupport.unwrapSearchHits(hits);
        return dtoList;
    }

    /**
     * 聚合查询最大值
     *
     * @param bo
     * @param esClass
     * @param <T>
     * @return
     */
    public <T extends EsBaseDTO> Long getMax(BaseBo bo, Class<T> esClass) {
        Aggregations aggregations = baseAgg(bo, esClass);
        if (aggregations == null) {
            return 0L;
        }
        Max max = aggregations.get(StatsTypeEnum.MAX.getCode());
        if (max != null) {
            return Math.round(max.getValue());
        }
        return 0L;
    }

    /**
     * 聚合查询数量
     *
     * @param bo
     * @param esClass
     * @param <T>
     * @return
     */
    public <T extends EsBaseDTO> long getCount(BaseBo bo, Class<T> esClass) {
        Query query = EsBeanConvert.convertQuery(bo, null);
        query.setTrackTotalHits(true);
        return template.count(query, esClass);
    }

    /**
     * 聚合查询数据
     *
     * @param bo
     * @param esClass
     * @param <T>
     * @return
     */
    public <T extends EsBaseDTO> Stats getStats(BaseBo bo, Class<T> esClass) {
        Aggregations aggregations = baseAgg(bo, esClass);
        if (aggregations == null) {
            return null;
        }
        Stats stats = aggregations.get(StatsTypeEnum.STATS.getCode());
        log.info("{}, 统计信息:{}", JSON.toJSONString(bo), JSON.toJSONString(stats));
        return stats;
    }

    /**
     * 分组查询
     *
     * @param reqOrBo
     * @param esClass
     * @param <T>
     * @return
     */
    public <T extends EsBaseDTO> Map<String, Long> groupBy(Object reqOrBo, Class<T> esClass) {
        Map<String, Long> groupMap = new HashMap<>(20);
        Aggregations aggregations = baseAgg(reqOrBo, esClass);
        if (aggregations == null) {
            return groupMap;
        }
        Terms stats = aggregations.get(StatsTypeEnum.GROUP.getCode());
        if (CollectionUtils.isNotEmpty(stats.getBuckets())) {
            for (Terms.Bucket bucket : stats.getBuckets()) {
                groupMap.put(bucket.getKey().toString(), bucket.getDocCount());
            }
        }
        log.info("{}, 分组统计个数:{}, 数据:{}", JSON.toJSONString(reqOrBo), groupMap.size(), groupMap.size() > 500 ? "……" : JSON.toJSONString(groupMap));
        return groupMap;
    }

    private <T extends EsBaseDTO> Aggregations baseAgg(Object reqOrBo, Class<T> esClass) {
        Query aggregationQuery = EsBeanConvert.convertAggregationQuery(reqOrBo, otherConfig.getExportLimit().intValue());
        // 执行查询
        SearchHits<T> searchResult = template.search(aggregationQuery, esClass);
        if (searchResult.hasAggregations()) {
            Aggregations aggregations = searchResult.getAggregations();
            return aggregations;
        }
        return null;
    }

    /**
     * ES通用分页查询
     *
     * @param bo      请求参数
     * @param voClass 响应参数class,注意要使用和存储对象一致的响应对象，不要包含其他索引对象的vo，否则可能会因为包含其他索引的include而查不到数据
     * @param esClass 索引类class
     * @return
     */
    public <T extends EsBaseDTO, K extends BaseVO> SearchHits<T> page(PageBO bo, Class<K> voClass, Class<T> esClass) {
        Query query = EsBeanConvert.convertQuery(bo, voClass);
        // 执行查询
        SearchHits<T> searchResult = template.search(query, esClass);
        log.info("查询到结果:{}, 总数:{}", searchResult.getSearchHits().size(), searchResult.getTotalHits());
        return searchResult;
    }

}
