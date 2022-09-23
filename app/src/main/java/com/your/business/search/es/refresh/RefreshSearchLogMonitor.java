package com.your.business.search.es.refresh;

import com.your.business.search.entity.SearchLog;
import com.your.business.search.es.dto.EsBaseDTO;
import com.your.business.search.es.dto.EsBaseEntity;
import com.your.business.search.es.dto.EsSearchLogDTO;
import com.your.business.search.message.EsFreshDelayProducer;
import com.your.business.search.message.param.RefreshDelayMsg;
import com.your.common.annotation.EsBinlog;
import com.your.common.util.EsBeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 前台搜索数据ES处理
 *
 * @author zhangzhen
 * @Date 2022/4/18 下午2:39
 */
@Slf4j
@Service
public class RefreshSearchLogMonitor extends RefreshBaseMonitor<SearchLog> {

    @Autowired
    private EsFreshDelayProducer delayProducer;

    @Override
    protected Class<? extends EsBaseDTO> getBindEs() {
        return EsSearchLogDTO.class;
    }

    @Override
    @EsBinlog(dml = BinlogDmlEnum.INSERT, tables = {SearchLog.class})
    public <K extends EsBaseEntity> void insertEntity(K msgEntity) {
        try {
            SearchLog entity = (SearchLog) msgEntity;
            boolean exists = queryVisitor.exists(entity.getVersion(), getBindEs());
            if (exists) {
                log.info("{}:{}, 数据已存在不再新增", getName(), entity.getVersion());
                return;
            }
            EsSearchLogDTO dto = new EsSearchLogDTO();
            dto.setVersion(entity.getVersion());
            dto.setLog(entity);
            writeVisitor.save(dto);
            log.info("新增{}记录:{}", getName(), entity.getVersion());
        } catch (Exception ex) {
            log.error("单对象新增出错:", ex);
        }
    }

    @Override
    @EsBinlog(dml = BinlogDmlEnum.DELETE, tables = {SearchLog.class})
    public void deleteEntity(SearchLog entity) {
        try {
            String result = writeVisitor.delete(entity.getVersion(), getBindEs());
            log.info("删除{}记录id:{}, 结果:{}", getName(), entity.getVersion(), result);
        } catch (Exception ex) {
            log.error("单对象删除出错:", ex);
        }
    }

    @Override
    @EsBinlog(dml = BinlogDmlEnum.UPDATE, tables = {SearchLog.class})
    public <K extends EsBaseEntity> void updateEntity(K entity) {
        try {
            SearchLog bean = (SearchLog) entity;
            // 比较时间
            EsSearchLogDTO dto = (EsSearchLogDTO) queryVisitor.getByDocId(bean.getVersion(), getBindEs());
            if (dto == null) {
                insertEntity(bean);
                return;
            }
            if (!EsBeanUtil.isUpdate(dto.getLog().getTs(), entity.getTs())) {
                log.warn("{}记录id:{}, ES数据修改时间大于消息的时间，不处理", getName(), bean.getVersion());
                return;
            }
            dto.setLog(bean);
            writeVisitor.save(dto);
            log.info("修改{}记录:{}", getName(), bean.getVersion());
        } catch (Exception ex) {
            log.error("单对象修改出错:", ex);
        }
    }

}
