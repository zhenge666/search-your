package com.your.business.search.job;

import com.your.business.search.es.EsStartVisitor;
import com.your.common.annotation.EsStart;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

/**
 * 刷新表数据任务
 *
 * @author zhangzhen
 * @Date 2022/5/10 下午5:08
 */
@Slf4j
@Service
public class TableDataInitJob {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    protected ThreadPoolTaskExecutor esStartExecutor;

    /**
     * 执行任务
     */
    @XxlJob("es-data-init")
    public void execute() {
        try {
            String param = XxlJobHelper.getJobParam();
            log.info("开始执行调度任务, 参数:{}", param);
            if (StringUtils.isBlank(param)) {
                return;
            }
            // 加载数据
            for (Class dto : EsStartVisitor.INDEX) {
                if (!dto.getSimpleName().equals(param)) {
                    continue;
                }
                EsStart start = (EsStart) dto.getAnnotationsByType(EsStart.class)[0];
                esStartExecutor.submit(() -> applicationContext.getBean(start.initClass()).initData(start.name()));
            }
            log.info("结束执行调度任务, 参数:{}", param);
        } catch (Exception ex) {
            log.error(BizErrorEnum.SYSTEM_ERROR + ",执行调度任务出错:", ex);
        }
    }

}
