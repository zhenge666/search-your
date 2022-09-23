package com.your.business.search.es.refresh;

import com.your.business.search.es.EsQueryVisitor;
import com.your.business.search.es.EsWriteVisitor;
import com.your.business.search.es.dto.EsBaseDTO;
import com.your.business.search.es.dto.EsBaseEntity;
import com.your.common.annotation.EsStart;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * ES初始化数据
 *
 * @author zhangzhen
 * @Date 2022/4/2 下午2:50
 */
@Service
@Slf4j
public abstract class RefreshBaseMonitor<T extends EsBaseEntity> {

    @Autowired
    protected EsQueryVisitor queryVisitor;

    @Autowired
    protected EsWriteVisitor writeVisitor;

    /**
     * 类命名
     *
     * @return
     */
    protected String getName() {
        EsStart start = getBindEs().getAnnotationsByType(EsStart.class)[0];
        if (start != null) {
            return start.name();
        }
        return null;
    }

    /**
     * 绑定的索引类
     *
     * @return
     */
    protected abstract Class<? extends EsBaseDTO> getBindEs();

    /**
     * 新增对象处理,入参是主对象
     *
     * @param msgEntity
     */
    public abstract <K extends EsBaseEntity> void insertEntity(K msgEntity);

    /**
     * 删除对象处理,入参是主对象
     *
     * @param entity
     */
    public abstract void deleteEntity(T entity);

    /**
     * 修改对象处理,入参可以是索引内的各个对象
     *
     * @param entity
     */
    public abstract <K extends EsBaseEntity> void updateEntity(K entity);

}