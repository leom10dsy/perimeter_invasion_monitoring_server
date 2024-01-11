package com.csrd.pims.bean.huawei.param;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.csrd.pims.tools.GsonUtil;
import com.csrd.pims.tools.XMLUtil;
import com.google.common.eventbus.Subscribe;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 华为订阅ivs1800报警
 */
@Data
@XStreamAlias("Content")
public class HuaweiIvsAlarmSubscribe {

    @XStreamAlias("Subscribe")
    private Subscribe subscribe;

    @Data
    @XStreamAlias("Subscribe")
    public static class Subscribe {
        @XStreamAlias("AddSubscribeList")
        private List<SubscribeInfo>  addSubscribeList;
        @XStreamAlias("DelSubscribeList")
        private List<SubscribeInfo>  delSubscribeList;
    }

    @Data
    @XStreamAlias("SubscribeInfo")
    public static class SubscribeInfo {

        @XStreamAlias("AlarmIncode")
        private String alarmInCode;
    }

    public static void main(String[] args) {
        HuaweiIvsAlarmSubscribe s = new HuaweiIvsAlarmSubscribe();
        Subscribe subscribe = new Subscribe();

        SubscribeInfo subscribeInfo = new SubscribeInfo();
        subscribeInfo.alarmInCode = "safasdsad";
        SubscribeInfo subscribeInfo2 = new SubscribeInfo();
        subscribeInfo2.alarmInCode = "safasdsad222";

        subscribe.addSubscribeList = new ArrayList<>();
        subscribe.addSubscribeList.add(subscribeInfo);
        subscribe.addSubscribeList.add(subscribeInfo2);
        s.subscribe = subscribe;

        String s1 = XMLUtil.beanToXml(s);
        System.out.println(s1);


    }
}
