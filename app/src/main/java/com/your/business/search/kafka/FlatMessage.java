package com.your.business.search.kafka;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @Author 666
 * @Create 2021/9/22 16:43
 * @Desc
 */
@ToString
@Data
public class FlatMessage implements Serializable {
    /**
     * uuid
     */
    private static final long serialVersionUID = 7911532126510050521L;

    /**
     * 数据库或schema
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
     * 表的字段 key：字段名称，value：字段类型
     */
    private Map<String, String> mysqlType;

    /**
     * 数据列表
     */
    private List<Map<String, Object>> data;
    /**
     * 旧数据列表, 用于update
     */
    private List<Map<String, Object>> old;

}
