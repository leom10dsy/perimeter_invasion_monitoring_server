package com.csrd.pims.config;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ClassUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 使@schedule支持多线程的配置类
 */

@Configuration
public class ScheduleConfig implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(pimsTaskScheduler());
    }

    @Bean
    public TaskScheduler pimsTaskScheduler() {
        Set<Class<?>> classes = ClassUtil.scanPackage("com.csrd.pims.schedule");
        List<Method> methods = new ArrayList<>();
        for (Class<?> aClass : classes) {
            methods.addAll(Arrays.asList(aClass.getMethods()));
        }
        int defaultPoolSize = 20;
        int corePoolSize = 0;
        if (CollectionUtil.isNotEmpty(methods)) {
            for (Method method : methods) {
                Scheduled annotation = method.getAnnotation(Scheduled.class);
                if (annotation != null) {
                    corePoolSize++;
                }
            }
        }
//        corePoolSize = corePoolSize * 2;
        if (defaultPoolSize < corePoolSize) {
            corePoolSize = defaultPoolSize;
        }

        ThreadPoolTaskScheduler executor = new ThreadPoolTaskScheduler();
        executor.setPoolSize(corePoolSize);
        executor.setThreadNamePrefix("pims-thread");
        //设置饱和策略
        //CallerRunsPolicy：线程池的饱和策略之一，当线程池使用饱和后，直接使用调用者所在的线程来执行任务；如果执行程序已关闭，则会丢弃该任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}