package com.csrd.pims.bean.huawei.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 华为NCE请求返回body
 * @param <T>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HuaweiNCEOutput<T> {
    private T output;
}
