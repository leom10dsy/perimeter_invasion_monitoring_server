package com.csrd.pims.bean.huawei.result;

import lombok.Data;

@Data
public class HuaweiPTZPresetInfo {
    /**
     * 预置位索引号
     */
    private String presetIndex;

    /**
     * 预置位名称
     */
    private String presetName;

    /**
     * 保留字段
     */
    private String reserve;

    /**
     * 对焦记忆开关 0-不启用，1-启用
     */
    private String focusSwitch;


}
