package com.your.business.search.es.init;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.your.business.search.es.dto.EsBaseEntity;
import com.your.common.config.EsInitConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * ES初始化数据
 *
 * @author zhangzhen
 * @Date 2022/4/2 下午2:50
 */
@Service
@Slf4j
public abstract class InitBaseCreator<T extends EsBaseEntity> {

    @Autowired
    protected EsInitConfig initConfig;

    @Autowired
    protected ThreadPoolTaskExecutor initDataExecutor;

    /**
     * 初始化数据
     *
     * @param dtoName 对象名称
     */
    public void initData(String dtoName) {
        Page<T> page = newPage();
        long total = 0;
        if (!isStart()) {
            log.info("不需要初始化{}数据", dtoName);
            return;
        }
        while (isStart()) {
            log.info("初始化{}数据, 范围:{}-->{}", dtoName, (page.getCurrent() - 1) * page.getSize(), page.getCurrent() * page.getSize());
            // 查询数据
            pageList(page);
            if (CollectionUtils.isNotEmpty(page.getRecords())) {
                // 记录处理数
                total += page.getRecords().size();
                // 多线程处理数据
                List<Future> futureList = new ArrayList(Integer.valueOf(page.getSize() + ""));
                for (T entity : page.getRecords()) {
                    entity.setTs(System.currentTimeMillis());
                    futureList.add(initDataExecutor.submit(() -> insertEntity(entity)));
                }
                for (Future future : futureList) {
                    try {
                        future.get();
                    } catch (Exception ex) {
                        log.error("多线程初始化数据单对象处理异常:{}", ex);
                        continue;
                    }
                }
            }
            if (page.getRecords().size() < page.getSize()) {
                log.info("初始化{}数据完成, 共处理数据:{}", dtoName, total);
                break;
            }
            // 下一页
            page.setCurrent(page.getCurrent() + 1);
            log.info("已处理{}数据:{}", dtoName, total);
        }
    }

    /**
     * 是否开始加载数据
     *
     * @return
     */
    protected boolean isStart() {
        return initConfig.getInitOpen();
    }

    /**
     * 新建分页对象
     *
     * @return
     */
    protected Page<T> newPage() {
        Page<T> page = new Page();
        page.setCurrent(initConfig.getFromCount() / initConfig.getPerSize() + 1);
        page.setSize(initConfig.getPerSize());
        return page;
    }

    /**
     * 分页获取数据
     *
     * @param page
     */
    protected abstract void pageList(Page<T> page);

    /**
     * 新增对象处理,入参是主对象
     *
     * @param entity
     */
    public abstract void insertEntity(T entity);

}