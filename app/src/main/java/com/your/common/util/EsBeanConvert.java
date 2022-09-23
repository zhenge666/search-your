package com.your.common.util;

import com.your.base.model.BaseBo;
import com.your.business.search.bo.PageBO;
import com.your.business.search.vo.BaseVO;
import com.your.common.annotation.EsResult;
import com.your.common.constant.EsConstant;
import com.your.product.center.facade.enums.YesNoEnum;
import com.your.search.center.facade.annotation.EsQuery;
import com.your.search.center.facade.annotation.EsStats;
import com.your.search.center.facade.enums.QueryTypeEnum;
import com.your.search.center.facade.enums.StatsTypeEnum;
import com.your.search.center.facade.req.BaseReq;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.PipelineAggregatorBuilders;
import org.elasticsearch.search.aggregations.pipeline.BucketSelectorPipelineAggregationBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilterBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.SourceFilter;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * ES类转换器
 *
 * @author zhangzhen
 * @Date 2022/4/8 下午3:15
 */
@Slf4j
public class EsBeanConvert {

    /**
     * bo入参对象转换为ES查询对象
     *
     * @param boOrReq controller的BO对象或facade的req对象
     * @return
     */
    public static <K extends BaseVO> Query convertQuery(Object boOrReq, Class<K> voClass) {
        // 初始化query对象
        NativeSearchQueryBuilder searchQuery = new NativeSearchQueryBuilder();
        // BO组装查询条件
        BoolQueryBuilder allQuery = QueryBuilders.boolQuery();
        boToQuery(boOrReq, boOrReq.getClass(), allQuery);
        searchQuery.withQuery(allQuery);
        // vo组装过滤字段和排序条件
        List<EsResult> esResults = new ArrayList<>();
        List<String> includeList = new ArrayList<>(100);
        if (voClass != null) {
            voToQuery(voClass, esResults);
        }
        if (esResults.size() > 1) {
            // 按照等级排序
            Collections.sort(esResults, new Comparator<EsResult>() {
                @Override
                public int compare(EsResult o1, EsResult o2) {
                    return o1.sort() - o2.sort();
                }
            });
        }
        for (EsResult esResult : esResults) {
            if (esResult.includes().length > 0) {
                Collections.addAll(includeList, esResult.includes());
            }
            // 设置排序
            if (esResult.sort() > 0 && StringUtils.isNotBlank(esResult.name())) {
                searchQuery.withSort(new FieldSortBuilder(esResult.name()).order(esResult.order()));
            }
        }
        // 过滤字段
        if (CollectionUtils.isNotEmpty(includeList)) {
            SourceFilter sourceFilter = new FetchSourceFilterBuilder().withIncludes(includeList.toArray(new String[includeList.size()])).build();
            searchQuery.withSourceFilter(sourceFilter);
        }
        // 使用分页
        if (boOrReq instanceof PageBO) {
            PageBO pageBO = (PageBO) boOrReq;
            // 约定0表示全部数据
            if (pageBO.getSize() > 0 && pageBO.getCurrent() > 0) {
                searchQuery.withPageable(PageRequest.of(pageBO.getCurrent().intValue() - 1, pageBO.getSize().intValue()));
            }
        }
        return searchQuery.build();
    }

    /**
     * bo入参对象转换为聚合查询对象
     *
     * @param boOrReq controller的BO对象或facade的req对象
     * @return
     */
    public static Query convertAggregationQuery(Object boOrReq, Integer maxCount) {
        NativeSearchQueryBuilder searchQuery = new NativeSearchQueryBuilder();
        // 组装查询条件
        BoolQueryBuilder allQuery = QueryBuilders.boolQuery();
        boToQuery(boOrReq, boOrReq.getClass(), allQuery);
        searchQuery.withQuery(allQuery);
        // 组装聚合条件
        boToAggregationQuery(boOrReq, searchQuery, maxCount);
        // 不需要返回结果
        searchQuery.withPageable(PageRequest.of(0, 1));
        return searchQuery.build();
    }

    /**
     * 拼装查询条件(查询参数平铺,如果后续确实太多，可以递归查询对象类型的属性)
     *
     * @param boOrReq      controller的BO对象或facade的req对象
     * @param boOrReqClass
     * @param allQuery     所有的查询
     */
    private static void boToQuery(Object boOrReq, Class boOrReqClass, BoolQueryBuilder allQuery) {
        // 如果是BaseBo本类不处理
        if (boOrReqClass.getName().equals(BaseBo.class.getName())
                || boOrReqClass.getName().equals(BaseReq.class.getName())) {
            return;
        }
        // 嵌套查询
        NestedQueryBuilder nestedQueryBuilder = null;
        BoolQueryBuilder nestedQuery = QueryBuilders.boolQuery();
        // 获取当前类的字段
        Field[] fields = boOrReqClass.getDeclaredFields();
        for (Field field : fields) {
            EsQuery query = field.getAnnotation(EsQuery.class);
            if (query == null) {
                continue;
            }
            Object value = EsBeanUtil.getFieldValue(boOrReq, field.getName());
            if (value == null || StringUtils.isBlank(value.toString())) {
                continue;
            }
            if (value instanceof Collection) {
                if (CollectionUtils.isEmpty((Collection) value)) {
                    continue;
                }
            }
            if (value instanceof LocalDateTime) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(EsConstant.TIME_FORMAT);
                value = formatter.format((LocalDateTime) value);
            }
            // 判断是整体查询还是嵌套查询
            BoolQueryBuilder nowQuery = null;
            String typeName = "常规查询";
            if (query.nestedOpen()) {
                if (nestedQueryBuilder == null) {
                    nestedQueryBuilder = QueryBuilders.nestedQuery(query.nestedPath(), nestedQuery, ScoreMode.None);
                }
                nowQuery = nestedQuery;
                typeName = "嵌套查询";
            } else {
                nowQuery = allQuery;
            }
            if (query.queryType().equals(QueryTypeEnum.TERMS)) {
                if (value instanceof Collection) {
                    Set<?> ids = EsBeanUtil.filterNull((Collection) value);
                    nowQuery.must(QueryBuilders.termsQuery(query.name(), ids.toArray()));
                    log.info("{}, 组装term条件:{}-->value个数:{}", typeName, query.name(), ids.size());
                } else {
                    nowQuery.must(QueryBuilders.termsQuery(query.name(), value));
                    log.info("{}, 组装term条件:{} = {}", typeName, query.name(), value);
                }
            } else if (query.queryType().equals(QueryTypeEnum.TERMS_FILTER)) {
                if (value instanceof Collection) {
                    Set<?> ids = EsBeanUtil.filterNull((Collection) value);
                    nowQuery.mustNot(QueryBuilders.termsQuery(query.name(), ids.toArray()));
                    log.info("{}, not in条件:{}-->value个数:{}", typeName, query.name(), ids.size());
                } else {
                    nowQuery.mustNot(QueryBuilders.termsQuery(query.name(), value));
                    log.info("{}, not in条件:{} = {}", typeName, query.name(), value);
                }
            } else if (query.queryType().equals(QueryTypeEnum.MATCH)) {
                nowQuery.must(QueryBuilders.matchQuery(query.name(), value));
                log.info("{}, 组装match条件:{} like {}", typeName, query.name(), value);
            } else if (query.queryType().equals(QueryTypeEnum.RANGE_GE)) {
                nowQuery.must(QueryBuilders.rangeQuery(query.name()).gte(value));
                log.info("{}, 组装条件:{} >= {}", typeName, query.name(), value);
            } else if (query.queryType().equals(QueryTypeEnum.RANGE_LE)) {
                nowQuery.must(QueryBuilders.rangeQuery(query.name()).lte(value));
                log.info("{}, 组装条件:{} <= {}", typeName, query.name(), value);
            } else if (query.queryType().equals(QueryTypeEnum.RANGE_GT)) {
                nowQuery.must(QueryBuilders.rangeQuery(query.name()).gt(value));
                log.info("{}, 组装条件:{} > {}", typeName, query.name(), value);
            } else if (query.queryType().equals(QueryTypeEnum.RANGE_LT)) {
                nowQuery.must(QueryBuilders.rangeQuery(query.name()).lt(value));
                log.info("{}, 组装条件:{} < {}", typeName, query.name(), value);
            } else if (query.queryType().equals(QueryTypeEnum.IDS)) {
                Set<?> ids = EsBeanUtil.filterNull((Collection) value);
                nowQuery.must(QueryBuilders.idsQuery().addIds(ids.toArray(new String[ids.size()])));
                log.info("{}, 组装ids条件:{}-->id个数:{}", typeName, query.name(), ids.size());
            } else if (query.queryType().equals(QueryTypeEnum.HAVE_OR_NO)) {
                if (Integer.valueOf(value.toString()).equals(YesNoEnum.YES.getDbValue())) {
                    nowQuery.must(QueryBuilders.rangeQuery(query.name()).gt(0));
                    log.info("{}, 组装条件:{} > 0", typeName, query.name());
                } else if (Integer.valueOf(value.toString()).equals(YesNoEnum.NO.getDbValue())) {
                    nowQuery.must(QueryBuilders.termsQuery(query.name(), "0"));
                    log.info("{}, 组装条件:{} = 0", typeName, query.name());
                }
            }
        }
        if (nestedQueryBuilder != null) {
            allQuery.must(nestedQueryBuilder);
        }
    }

    /**
     * 聚合查询条件
     *
     * @param boOrReq     controller的BO对象或facade的req对象
     * @param searchQuery
     */
    private static void boToAggregationQuery(Object boOrReq, NativeSearchQueryBuilder searchQuery, Integer maxCount) {
        // 获取当前类的字段
        Field[] fields = boOrReq.getClass().getDeclaredFields();
        for (Field field : fields) {
            EsStats stats = field.getAnnotation(EsStats.class);
            if (stats == null) {
                continue;
            }
            AbstractAggregationBuilder builder = null;
            if (stats.type().equals(StatsTypeEnum.MAX)) {
                builder = AggregationBuilders.max(StatsTypeEnum.MAX.getCode()).field(stats.fieldName());
            } else if (stats.type().equals(StatsTypeEnum.MIN)) {
                builder = AggregationBuilders.min(StatsTypeEnum.MIN.getCode()).field(stats.fieldName());
            } else if (stats.type().equals(StatsTypeEnum.SUM)) {
                builder = AggregationBuilders.sum(StatsTypeEnum.SUM.getCode()).field(stats.fieldName());
            } else if (stats.type().equals(StatsTypeEnum.AVG)) {
                builder = AggregationBuilders.avg(StatsTypeEnum.AVG.getCode()).field(stats.fieldName());
            } else if (stats.type().equals(StatsTypeEnum.COUNT)) {
                builder = AggregationBuilders.count(StatsTypeEnum.COUNT.getCode()).field(stats.fieldName());
            } else if (stats.type().equals(StatsTypeEnum.STATS)) {
                builder = AggregationBuilders.stats(StatsTypeEnum.STATS.getCode()).field(stats.fieldName());
            } else if (stats.type().equals(StatsTypeEnum.GROUP)) {
                builder = AggregationBuilders.terms(StatsTypeEnum.GROUP.getCode()).field(stats.fieldName()).size(maxCount);
            } else if (stats.type().equals(StatsTypeEnum.Having)) {
                builder = AggregationBuilders.terms(StatsTypeEnum.GROUP.getCode()).field(stats.fieldName()).size(maxCount);
                Script script = createHaving(boOrReq, stats);
                if (script != null) {
                    Map<String, String> bucketsPathsMap = new HashMap<>(10);
                    bucketsPathsMap.put(StatsTypeEnum.COUNT.getCode(), "_count");
                    BucketSelectorPipelineAggregationBuilder bs = PipelineAggregatorBuilders.bucketSelector(StatsTypeEnum.Having.getCode(), bucketsPathsMap, script);
                    builder.subAggregation(bs);
                }
            }
            if (builder != null) {
                searchQuery.addAggregation(builder);
            }
        }
    }

    /**
     * 获取过滤字段和排序条件(向上向下递归)
     *
     * @param voClass
     * @param esResults
     */
    private static void voToQuery(Class voClass, List<EsResult> esResults) {
        // 如果是BaseVO本类不处理
        if (voClass.getName().equals(BaseVO.class.getName())) {
            return;
        }
        // 如果父类不是BaseVO本类递归处理
        if (!voClass.getSuperclass().getName().equals(BaseVO.class.getName())) {
            voToQuery(voClass.getSuperclass(), esResults);
        }
        // 获取当前类的字段
        Field[] fields = voClass.getDeclaredFields();
        for (Field field : fields) {
            if (BaseVO.class.isAssignableFrom(field.getType())) {
                if (!field.getType().getName().equals(BaseVO.class.getName())) {
                    voToQuery(field.getType(), esResults);
                    continue;
                }
            }
            EsResult result = field.getAnnotation(EsResult.class);
            if (result == null) {
                continue;
            }
            esResults.add(result);
        }
    }

    /**
     * vo中找到指定的字段
     *
     * @param voClass
     * @return
     */
    public static List<String> voFindField(Class voClass) {
        List<String> includeList = new ArrayList<>(20);
        // 获取当前类的字段
        Field[] fields = voClass.getDeclaredFields();
        for (Field field : fields) {
            EsResult result = field.getAnnotation(EsResult.class);
            if (result == null) {
                continue;
            }
            if (result.includes().length > 0) {
                Collections.addAll(includeList, result.includes());
            }
        }
        return includeList;
    }

    private static Script createHaving(Object boOrReq, EsStats stats) {
        // 拼装字符串示例:"params.orderCount >= 10 && params.orderCount <= 40";
        StringBuilder append = new StringBuilder();
        if (StringUtils.isNotBlank(stats.startField())) {
            Object value = EsBeanUtil.getFieldValue(boOrReq, stats.startField());
            if (value != null && StringUtils.isNotBlank(value.toString())) {
                append.append("params.").append(StatsTypeEnum.COUNT.getCode()).append(" >= ").append(value);
            }
        }
        if (StringUtils.isNotBlank(stats.endField())) {
            Object value = EsBeanUtil.getFieldValue(boOrReq, stats.endField());
            if (value != null && StringUtils.isNotBlank(value.toString())) {
                if (append.length() > 0) {
                    append.append(" && ");
                }
                append.append("params.").append(StatsTypeEnum.COUNT.getCode()).append(" <= ").append(value);
            }
        }
        if (append.length() > 0) {
            return new Script(append.toString());
        }
        return null;
    }

}
