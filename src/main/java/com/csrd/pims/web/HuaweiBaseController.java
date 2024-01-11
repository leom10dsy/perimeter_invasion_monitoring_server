package com.csrd.pims.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csrd.pims.bean.web.ResultWrapper;
import com.csrd.pims.dao.entity.nce.HuaweiNceAlarmDistance;
import com.csrd.pims.dao.mapper.HuaweiNceAlarmDistanceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


/**
 * 华为报警处理
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/huawei/base")
public class HuaweiBaseController {

    @Autowired
    private HuaweiNceAlarmDistanceMapper distanceMapper;


    @RequestMapping("testSse")
    @ResponseBody
    public ResultWrapper<String> testSse() {
        return ResultWrapper.success("=== sseTest: ");
    }

    @ResponseBody
    @PostMapping("distance")
    public ResultWrapper<String> getDistance(@RequestParam("distance") Long distance) {

        Float result = 0f;
        LambdaQueryWrapper<HuaweiNceAlarmDistance> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(HuaweiNceAlarmDistance::getBegin, distance);
        wrapper.gt(HuaweiNceAlarmDistance::getEnd, distance);
        HuaweiNceAlarmDistance alarmDistance = distanceMapper.selectOne(wrapper);

        if (alarmDistance == null) {
        } else if (alarmDistance.getCoefficient() == 0) {
            result = alarmDistance.getBaseDistance();
        } else {
            result = alarmDistance.getBaseDistance() + alarmDistance.getCoefficient() * (distance - alarmDistance.getBegin());
        }
        return ResultWrapper.success(Math.round(result) + "");
    }

    @ResponseBody
    @PostMapping("distanceList")
    public ResultWrapper<Map<Integer, String>> getDistance(@RequestParam("begin") int begin, @RequestParam("end") int end) {
        HashMap<Integer, String> map = new HashMap<>();
        for (int i = begin; i <= end; i++) {
            Float result = 0f;
            LambdaQueryWrapper<HuaweiNceAlarmDistance> wrapper = new LambdaQueryWrapper<>();
            wrapper.le(HuaweiNceAlarmDistance::getBegin, i);
            wrapper.gt(HuaweiNceAlarmDistance::getEnd, i);
            HuaweiNceAlarmDistance alarmDistance = distanceMapper.selectOne(wrapper);

            if (alarmDistance == null) {
            } else if (alarmDistance.getCoefficient() == 0) {
                result = alarmDistance.getBaseDistance();
            } else {
                result = alarmDistance.getBaseDistance() + alarmDistance.getCoefficient() * (i - alarmDistance.getBegin());
            }
            map.put(i, Math.round(result) + "");
        }


        return ResultWrapper.success(map);
    }


}