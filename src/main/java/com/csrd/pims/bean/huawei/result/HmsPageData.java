package com.csrd.pims.bean.huawei.result;

import lombok.Data;

/**
 * 分页信息
 */
@Data
public class HmsPageData<T> {
    private int pageSize;
    private int pageNum;
    private int total;
    private T data;
}
