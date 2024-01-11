package com.csrd.pims.enums;

import lombok.Getter;

/**
 * 铁科系统类型：华为毫米波、nce、铁发等
 */
@Getter
public enum TkSysTypeEnum {

    IVS("ivs"),
    NCE("nce");

    TkSysTypeEnum(String type) {
        this.type = type;
    }
    private final String type;

}
