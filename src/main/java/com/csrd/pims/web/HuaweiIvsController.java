package com.csrd.pims.web;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.csrd.pims.bean.huawei.param.PTZControlModel;
import com.csrd.pims.bean.huawei.result.HuaweiPTZPresetInfo;
import com.csrd.pims.bean.web.ResultWrapper;
import com.csrd.pims.service.HuaweiIvsService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * &#064;ClassName:  HuaWeiIvsController
 * &#064;Description:
 */
@RestController
@RequestMapping("ivs")
public class HuaweiIvsController {


    @Resource
    private HuaweiIvsService huaweiIvsService;

    @PostMapping("intelligentAlarmCallback")
    public ResultWrapper<String> alarmCallback(@RequestBody String callbackVo) {
        if (ObjectUtil.isNotNull(callbackVo)) {
            huaweiIvsService.huaweiIvsIntelligentAlarmHandle(callbackVo);
        }

        return ResultWrapper.HuaweiOk("");
    }

    @GetMapping("realRtspurl")
    @ApiOperation(value = "华为实时视频url")
    public ResultWrapper<String> getRealRtspurl(@RequestParam("cameraNumber") String cameraNumber) {
        String result = huaweiIvsService.getRealRtspurl(cameraNumber);
        if (StrUtil.isEmpty(result)) {
            return ResultWrapper.error("未登录ivs设备或者其它错误");
        } else {
            return ResultWrapper.success(result);
        }

    }

    @GetMapping("stream")
    @ApiOperation(value = "华为实时视频url")
    public ResultWrapper<String> getStream(@RequestParam("ip") String ip, @RequestParam("port") String port,
                                           @RequestParam("cameraNumber") String cameraNumber, @RequestParam("type") int type,
                                           @RequestParam(value = "alarmTime" , required = false) String alarmTime) {
        return ResultWrapper.success(huaweiIvsService.getStream(ip, port, cameraNumber,
                type, DateUtil.parse(alarmTime , DatePattern.NORM_DATETIME_PATTERN)));

    }



}
