package com.your.business.search.es.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * es中用到的数据库对象entity扩展类
 *
 * @author zhangzhen
 * @Date 2022/4/19 下午5:47
 */
@Data
public class EsBaseEntity implements Serializable {

    /**
     * binlog的数据毫秒时间戳
     */
    @TableField(exist = false)
    private Long ts;

}
