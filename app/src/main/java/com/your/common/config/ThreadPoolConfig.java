package com.your.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 业务处理线程池配置
 *
 * @author zhangzhen
 * @Date 2022/4/2 下午2:50
 */
@Configuration
public class ThreadPoolConfig {

    /**
     * 初始化批量处理单个对象的线程池
     *
     * @return
     */
    @Bean("initDataExecutor")
    public ThreadPoolTaskExecutor initDataExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        executor.setCorePoolSize(5);
        // 设置最大线程数
        executor.setMaxPoolSize(30);
        // 设置队列容量
        executor.setQueueCapacity(0);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(60);
        // 设置默认线程名称
        executor.setThreadNamePrefix("init-data-");
        // 设置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        return executor;
    }

    /**
     * 处理多个对象的数据线程池
     *
     * @return
     */
    @Bean("esStartExecutor")
    public ThreadPoolTaskExecutor esStartExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        executor.setCorePoolSize(1);
        // 设置最大线程数
        executor.setMaxPoolSize(10);
        // 设置队列容量
        executor.setQueueCapacity(0);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(60);
        // 设置默认线程名称
        executor.setThreadNamePrefix("es-start-");
        // 设置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        return executor;
    }

    /**
     * mq消息重发线程
     *
     * @return 线程池
     */
    @Bean(destroyMethod = "shutdown")
    public ScheduledThreadPoolExecutor mqExecutor() {
        // 指定最大待执行数量，防止消息过度堆积
        int max = 1000;
        return new ScheduledThreadPoolExecutor(2, new ThreadPoolExecutor.AbortPolicy()) {
            @Override
            public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
                // 如果待处理数量超过指定最大值，则执行既定策略（抛出异常，异常捕捉那有告警）
                if (getQueue().size() > max) {
                    getRejectedExecutionHandler().rejectedExecution(command, this);
                }
                return super.schedule(command, delay, unit);
            }
        };
    }

}
