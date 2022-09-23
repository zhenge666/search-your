package com.your.business.search.es;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.your.business.search.es.dto.EsBaseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.elasticsearch.core.query.UpdateResponse;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * ES写入通用类
 *
 * @author zhangzhen
 * @Date 2022/4/20 上午11:01
 */
@Service
@Slf4j
public class EsWriteVisitor {

    @Autowired
    private ElasticsearchRestTemplate template;

    /**
     * 更新索引部分对象
     *
     * @param id
     * @param beanMap
     * @param esClass
     */
    public <T extends EsBaseDTO> void updateBean(String id, Map<String, Object> beanMap, Class<T> esClass) {
        String json = JSON.toJSONString(beanMap, SerializerFeature.WriteMapNullValue);
        Document doc = Document.parse(json);
        UpdateQuery updateQuery = UpdateQuery.builder(id).withDocument(doc).build();
        UpdateResponse response = template.update(updateQuery, template.getIndexCoordinatesFor(esClass));
        log.info("{}, id:{}, 更新结果:{}", esClass.getSimpleName(), id, JSON.toJSONString(response));
    }

    /**
     * 保存对象
     *
     * @param esDTO
     * @param <T>
     */
    public <T extends EsBaseDTO> void save(T esDTO) {
        template.save(esDTO);
    }

    /**
     * 删除对象
     *
     * @param id
     * @param esClass
     * @param <T>
     */
    public <T extends EsBaseDTO> String delete(String id, Class<T> esClass) {
        String result = template.delete(id, esClass);
        return result;
    }

}
