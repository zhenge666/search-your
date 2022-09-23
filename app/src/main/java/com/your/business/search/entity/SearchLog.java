package com.your.business.search.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.your.business.search.es.dto.EsBaseEntity;
import com.your.common.constant.EsConstant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

/**
 * <p>
 * 搜索日志表
 * </p>
 *
 * @author 666
 * @since 2022-04-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SearchLog extends EsBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Field(type = FieldType.Long)
    private Long id;

    /**
     * 搜索标志
     */
    @Field(type = FieldType.Keyword)
    private String version;

    /**
     * 来源ip
     */
    @Field(type = FieldType.Ip)
    private String ip;

    /**
     * 用户id
     */
    @Field(type = FieldType.Long)
    private Long userId;

    /**
     * 一句话描述
     */
    @TableField(value = "`desc`")
    @Field(type = FieldType.Text, analyzer = "ik_smart", searchAnalyzer = "ik_smart")
    private String desc;

    /**
     * 主标题
     */
    @Field(type = FieldType.Text, analyzer = "ik_smart", searchAnalyzer = "ik_smart")
    private String title;

    /**
     * 副标题
     */
    @Field(type = FieldType.Text, analyzer = "ik_smart", searchAnalyzer = "ik_smart")
    private String subtitle;

    /**
     * 二级平台appId
     */
    @Field(type = FieldType.Keyword)
    private String appId;

    /**
     * 一级行业id: industry.id
     */
    @Field(type = FieldType.Long)
    private Long industryId;

    /**
     * 一级行业名称
     */
    @Field(type = FieldType.Keyword)
    private String industry;

    /**
     * 二级行业id
     */
    @Field(type = FieldType.Long)
    private Long secondIndustryId;

    /**
     * 二级行业名称
     */
    @Field(type = FieldType.Keyword)
    private String secondIndustry;

    /**
     * 分词
     */
    @Field(type = FieldType.Text, analyzer = "ik_smart", searchAnalyzer = "ik_smart")
    private String words;

    /**
     * 扩展信息
     */
    @Field(type = FieldType.Text, analyzer = "ik_smart", searchAnalyzer = "ik_smart")
    private String extension;

    /**
     * 创建时间
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    @JSONField(format = EsConstant.TIME_FORMAT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis, index = false)
    @JSONField(format = EsConstant.TIME_FORMAT)
    private LocalDateTime updateTime;

}
