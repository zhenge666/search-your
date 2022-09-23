package com.your.business.search.kafka;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.annotation.TableField;
import com.your.business.search.es.dto.EsBaseEntity;
import com.your.business.search.es.refresh.RefreshBaseMonitor;
import com.your.business.search.push.PushTaskCreator;
import com.your.common.enums.BinlogDmlEnum;
import com.your.common.util.EsBeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 数据监听入口
 *
 * @author zhangzhen
 * @Date 2022/4/16 下午4:30
 */
@Slf4j
@Component
public class BinlogKafkaListener {

    @Autowired
    protected ThreadPoolTaskExecutor initDataExecutor;

    /**
     * key:库名:表名:操作类型, value:数据处理业务类
     */
    public static ConcurrentMap<String, RefreshBaseMonitor> monitorMap = new ConcurrentHashMap();

    /**
     * key:库名:表名, value:数据库entity类
     */
    public static ConcurrentMap<String, Class<? extends EsBaseEntity>> entityMap = new ConcurrentHashMap();

    /**
     * 监听消息
     *
     * @param record
     */
    @KafkaListener(topics = "${config.topic}")
    private void listener(ConsumerRecord<String, String> record) {
        try {
            long startTime = System.currentTimeMillis();
            log.info("监听消息:{}", record);
            if (StringUtils.isBlank(record.value())) {
                log.info("消息value为空，不处理");
                return;
            }
            // binlog解析数据库对象
            // FlatMessage message = BinlogMessageUtil.flatMessage2Dml(JSON.parseObject(record.value(), KafkaFlatMessage.class), true);
            if (message.getIsDdl() || StringUtils.isBlank(message.getDatabase())) {
                log.info("ddl操作不处理:{},database:{}", message.getIsDdl(), message.getDatabase());
                return;
            }
            BinlogDmlEnum typeEnum = BinlogDmlEnum.getByValue(message.getType());
            if (typeEnum == null) {
                log.warn("操作类型不属于新增、修改、删除:{}，", message.getType());
                return;
            }
            String entityKey = createEntityKey(message.getDatabase(), message.getTable());
            Class<? extends EsBaseEntity> entityClass = entityMap.get(entityKey);
            if (entityClass == null) {
                log.info("{}, 没有找到对应的entity类，不处理", entityKey);
                return;
            }
            // 触发监测表是否有字段变更
            checkTableField(message, entityClass);
            // binlog解析实体对象
            // List<? extends EsBaseEntity> nowList = BinlogConvertUtil.mapsToList(message.getData(), entityClass);
            log.info("本次监听数据库:{}, 表:{}, 消息数量:{}", message.getDatabase(), message.getTable(), nowList.size());
            String monitorKey = createMonitorKey(message.getDatabase(), message.getTable(), typeEnum.getValue());
            RefreshBaseMonitor monitor = monitorMap.get(monitorKey);
            if (monitor == null) {
                log.info("{}, 没有找到对应的处理类，不处理", monitorKey);
                return;
            }
            List<Future> futureList = new ArrayList(nowList.size());
            switch (typeEnum) {
                case INSERT:
                    for (EsBaseEntity entity : nowList) {
                        entity.setTs(message.getTs());
                        futureList.add(initDataExecutor.submit(() -> monitor.insertEntity(entity)));
                    }
                    break;
                case DELETE:
                    for (EsBaseEntity entity : nowList) {
                        futureList.add(initDataExecutor.submit(() -> monitor.deleteEntity(entity)));
                    }
                    break;
                case UPDATE:
                    for (EsBaseEntity entity : nowList) {
                        entity.setTs(message.getTs());
                        futureList.add(initDataExecutor.submit(() -> monitor.updateEntity(entity)));
                    }
                    break;
                default:
                    log.error("{}, 不会有其他操作:{}", typeEnum, BizErrorEnum.SYSTEM_ERROR.writeLog());
                    break;
            }
            for (Future future : futureList) {
                try {
                    future.get();
                } catch (Exception ex) {
                    log.error("多线程处理binlog异常:{}", ex);
                    continue;
                }
            }
            log.info("结束消费: topic={}, partition={}, offset={}, 耗时={}ms", record.topic(),
                    record.partition(), record.offset(), System.currentTimeMillis() - startTime);
        } catch (Exception ex) {
            log.warn("监听数据错误:{}，发出预警:{}", ex.getMessage(), BizErrorEnum.SYSTEM_ERROR.writeLog());
            log.error("数据监听出错:", ex);
        }
    }

    private void checkTableField(FlatMessage message, Class entityClass) {
        Field[] fields = entityClass.getDeclaredFields();
        StringBuilder builder = new StringBuilder();
        Set<String> fieldSet = new HashSet<>(fields.length);
        for (Field field : fields) {
            if (field.getName().contains("serialVersionUID")) {
                continue;
            }
            String name = null;
            TableField tableField = field.getAnnotation(TableField.class);
            if (tableField != null && StringUtils.isNotBlank(tableField.value())) {
                name = tableField.value().replace("`", "");
            } else {
                name = EsBeanUtil.camelToUnderline(field.getName());
            }
            fieldSet.add(name);
            if (!message.getMysqlType().containsKey(name)) {
                builder.append("-删除字段:").append(field.getName()).append(",");
            }
        }
        for (Map.Entry<String, String> entry : message.getMysqlType().entrySet()) {
            if (!fieldSet.contains(entry.getKey())) {
                builder.append("-增加字段:").append(entry.getKey()).append(":").append(entry.getValue());
            }
        }
        if (builder.length() > 0) {
            log.info("table:{}, 表字段有修改:{}, 发出预警:{}", message.getTable(), builder.toString(), BizErrorEnum.SYSTEM_ERROR.writeLog());
        }
    }

    public static String createEntityKey(String dbName, String tableName) {
        return String.format("%s:%s", dbName, tableName);
    }

    public static String createMonitorKey(String dbName, String tableName, String dml) {
        return String.format("%s:%s:%s", dbName, tableName, dml);
    }

}
