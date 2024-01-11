package com.csrd.pims.bean.alarm;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.File;

/**
 * @description:
 * @author: shiwei
 * @create: 2022-07-07 14:01:57
 **/
@Data
@Accessors(chain = true)
public class AlarmFileInfo {

    private String deviceId;

    private File SrcFilE;

}
