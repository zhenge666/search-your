package com.your.business.search.es.init;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.your.business.search.entity.SearchLog;
import com.your.business.search.es.refresh.RefreshSearchLogMonitor;
import com.your.business.search.service.SearchLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 前台搜索数据ES处理
 *
 * @author zhangzhen
 * @Date 2022/4/18 下午2:39
 */
@Slf4j
@Service
public class InitSearchLogCreator extends InitBaseCreator<SearchLog> {

    @Autowired
    private SearchLogService logService;

    @Autowired
    private RefreshSearchLogMonitor monitor;

    @Override
    protected void pageList(Page<SearchLog> page) {
        logService.allPage(page);
    }

    @Override
    public void insertEntity(SearchLog entity) {
        monitor.insertEntity(entity);
    }

}
