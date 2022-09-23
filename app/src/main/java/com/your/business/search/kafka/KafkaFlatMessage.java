package com.your.business.search.kafka;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @Author 666
 * @Create 2021/9/22 11:39
 * @Desc
 */
@NoArgsConstructor
@Data
public class KafkaFlatMessage implements Serializable {

    /**
     * uuid
     */
    private static final long serialVersionUID = -4505583230915692964L;
    /**
     * Kafka 消息 offset
     */
    private long offset;

    /**
     * id
     */
    private long id;

    /**
     * 数据库
     */
    private String database;
    /**
     * 表
     */
    private String table;
    /**
     * 主键
     */
    private List<String> pkNames;
    /**
     * ddl
     */
    private Boolean isDdl;
    /**
     * DML
     */
    private String type;
    /**
     * binlog executeTime
     */
    private Long es;
    /**
     * dml build timeStamp
     */
    private Long ts;
    /**
     * sql
     * <p>
     * 执行的sql, dml sql为空
     */
    private String sql;

    /**
     * sqlType
     */
    private Map<String, Integer> sqlType;
    /**
     * mysqlType
     */
    private Map<String, String> mysqlType;
    /**
     * 数据列表
     */
    private List<Map<String, String>> data;

    /**
     * 旧数据列表, 用于update
     */
    private List<Map<String, String>> old;

}
