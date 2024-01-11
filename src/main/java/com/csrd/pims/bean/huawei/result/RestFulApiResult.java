package com.csrd.pims.bean.huawei.result;

public class RestFulApiResult<T> {
    private int resultCode;
    private T data;

    RestFulApiResult(int resultCode) {
        this.resultCode = resultCode;
    }

    public RestFulApiResult(int resultCode, T data) {
        this.resultCode = resultCode;
        this.data = data;
    }

    public static RestFulApiResult code(int code) {
        return data(code, null);
    }

    public static RestFulApiResult data(int code, Object data) {
        return new RestFulApiResult<>(code, data);
    }

    public int getCode() {
        return resultCode;
    }

    public void setCode(int code) {
        this.resultCode = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Result{" +
                "code=" + resultCode +
                ", data=" + data +
                '}';
    }
}
