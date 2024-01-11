package com.csrd.pims.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csrd.pims.amqp.tk.TKAlarmInfo;
import org.springframework.stereotype.Repository;

@Repository
public interface TkAlarmMapper extends BaseMapper<TKAlarmInfo> {
}
