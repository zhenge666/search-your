package com.your.business.search.es;

import com.alibaba.fastjson.JSON;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.service.IService;
import com.your.business.search.es.dto.*;
import com.your.business.search.es.refresh.RefreshBaseMonitor;
import com.your.business.search.kafka.BinlogKafkaListener;
import com.your.common.annotation.EsBinlog;
import com.your.common.util.EsBeanCheck;
import com.your.common.util.EsBeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.settings.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ES启动加载数据
 *
 * @author zhangzhen
 * @Date 2022/4/2 下午2:50
 */
@Service
@Slf4j
@Order(value = 1)
public class EsStartVisitor implements CommandLineRunner {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ElasticsearchRestTemplate template;

    @Autowired
    private EsOtherConfig otherConfig;

    public static List<Class> INDEX = new ArrayList<>(10);

    static {
        INDEX.add(EsSearchLogDTO.class);
    }

    /**
     * 初始化索引
     */
    @Override
    public void run(String... args) throws Exception {
        log.info("启动创建索引");
        // 校验字段
        for (Class dto : INDEX) {
            EsBeanCheck.checkFiled(dto);
        }
        String max = "index.max_result_window";
        // 创建索引
        for (Class dto : INDEX) {
            String indexName = ((Document) dto.getAnnotationsByType(Document.class)[0]).indexName();
            // 检查索引存不存在，不存在则创建索引
            boolean exist = template.indexOps(dto).exists();
            log.info("索引:{}, 是否存在:{}", indexName, exist);
            if (!exist) {
                boolean create = template.indexOps(dto).create();
                boolean mapping = template.indexOps(dto).putMapping(template.indexOps(dto).createMapping(dto));
                log.info("索引:{}, 创建索引:{}, 创建结构:{}", indexName, create, mapping);
            }
            log.info("索引:{}, setting:{}", indexName, template.indexOps(dto).getSettings());
            log.info("索引:{}, mapping:{}", indexName, template.indexOps(dto).getMapping());
            // 设置导出数量限制
            Object maxLimit = template.indexOps(dto).getSettings(true).get(max);
            if (maxLimit == null || Long.valueOf(maxLimit.toString()) < otherConfig.getExportLimit()) {
                Settings settings = Settings.builder().put(max, otherConfig.getExportLimit()).build();
                UpdateSettingsRequest settingsRequest = new UpdateSettingsRequest(settings, indexName);
                AcknowledgedResponse response = template.execute((client) -> {
                    return client.indices().putSettings(settingsRequest, RequestOptions.DEFAULT);
                });
                log.info("索引:{}, 修改最大查询数量结果:{}", indexName, JSON.toJSONString(response));
            }
        }
    }

    @PostConstruct
    public void beanLoader() {
        Map<String, RefreshBaseMonitor> esMap = applicationContext.getBeansOfType(RefreshBaseMonitor.class);
        log.info("找到数据访问者:{}个", esMap.size());
        Map<String, String[]> tableMap = tableMapping();
        for (RefreshBaseMonitor visitor : new ArrayList<>(esMap.values())) {
            Class visitorClass = EsBeanCheck.getBeanBySpring(visitor.getClass());
            Method[] methods = visitorClass.getDeclaredMethods();
            for (Method method : methods) {
                EsBinlog esBinlog = method.getAnnotation(EsBinlog.class);
                if (esBinlog == null) {
                    continue;
                }
                for (Class entity : esBinlog.tables()) {
                    String[] table = tableMap.get(entity.getName());
                    if (table == null) {
                        throw new RuntimeException(String.format("类:%s,方法:%s,注解:%s,找不到对应的数据库对象", visitorClass.getSimpleName(), method.getName(), entity.getSimpleName()));
                    }
                    String monitorKey = BinlogKafkaListener.createMonitorKey(table[0], table[1], esBinlog.dml().getValue());
                    if (!BinlogKafkaListener.monitorMap.containsKey(monitorKey)) {
                        BinlogKafkaListener.monitorMap.put(monitorKey, visitor);
                    }
                    String entityKey = BinlogKafkaListener.createEntityKey(table[0], table[1]);
                    if (!BinlogKafkaListener.entityMap.containsKey(entityKey)) {
                        BinlogKafkaListener.entityMap.put(entityKey, entity);
                    }
                    log.info("库:{}, 表:{}, 操作:{}, 访问类:{}", table[0], table[1], esBinlog.dml().getValue(), visitorClass.getSimpleName());
                }
            }
        }
    }

    /**
     * 返回数据表的映射
     *
     * @return key:数据库entity对象class全名称，value：库名、表名
     */
    public Map<String, String[]> tableMapping() {
        Map<String, IService> serviceMap = applicationContext.getBeansOfType(IService.class);
        log.info("找到service:{}个", serviceMap.size());
        Map<String, String[]> tableMap = new HashMap<>(serviceMap.size());
        for (IService service : new ArrayList<>(serviceMap.values())) {
            Class serviceClass = EsBeanCheck.getBeanBySpring(service.getClass());
            DS ds = (DS) serviceClass.getAnnotation(DS.class);
            if (ds == null) {
                throw new RuntimeException(String.format("类:%s,没有配置DS数据源", serviceClass.getSimpleName()));
            }
            String tableName = null;
            TableName table = (TableName) service.getEntityClass().getAnnotation(TableName.class);
            if (table == null) {
                // 驼峰转下划线
                tableName = EsBeanUtil.camelToUnderline(service.getEntityClass().getSimpleName());
            } else {
                tableName = table.value();
            }
            tableMap.put(service.getEntityClass().getName(), new String[]{ds.value(), tableName});
            log.info("库名:{}, 表名:{}, entity:{}", ds.value(), tableName, service.getEntityClass().getSimpleName());
        }
        return tableMap;
    }

}
