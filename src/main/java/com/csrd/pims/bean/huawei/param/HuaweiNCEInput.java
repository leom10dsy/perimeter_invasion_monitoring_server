package com.csrd.pims.bean.huawei.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 华为NCE请求入参
 * @param <T>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HuaweiNCEInput<T> {
    private T input;
}
