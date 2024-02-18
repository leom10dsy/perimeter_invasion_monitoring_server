package com.csrd.pims.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csrd.pims.bean.huawei.HuaweiVideoQueue;
import com.csrd.pims.dao.mapper.HuaweiVideoQueueMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 华为ivs定时任务业务操作类
 */
@Slf4j
@Component
public class HuaweiIvsScheduleHandler {

    @Resource
    private HuaweiVideoQueueMapper huaweiVideoQueueMapper;


    public List<HuaweiVideoQueue> downloadVideo(int size) {
        LambdaQueryWrapper<HuaweiVideoQueue> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderBy(true, true, HuaweiVideoQueue::getAlarmTime);
        wrapper.eq(HuaweiVideoQueue::getIsDownload, 0);
        wrapper.last("limit " + size);
        return huaweiVideoQueueMapper.selectList(wrapper);
    }
}
