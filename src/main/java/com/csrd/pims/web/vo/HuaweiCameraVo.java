package com.csrd.pims.web.vo;

import com.csrd.pims.dao.entity.ivs.HuaweiCamera;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HuaweiCameraVo extends HuaweiCamera {

    private String ivsIp;

    private String ivsPort;

    private String ivsNumber;

    private String ivsName;

    private String cookie;


}
