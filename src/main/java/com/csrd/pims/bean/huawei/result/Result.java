package com.csrd.pims.bean.huawei.result;

public class Result<T> {
    private int code;
    private T data;
    Result(int code) {
        this.code = code;
    }
    public Result(int code, T data) {
        this.code = code;
        this.data = data;
    }

    public static Result code(int code) {
        return data(code, null);
    }
    public static Result data(int code, Object data) {
        return new Result<>(code, data);
    }
    public int getCode() {
        return code;
    }
    public void setCode(int code) {
        this.code = code;
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
                "code=" + code +
                ", data=" + data +
                '}';
    }
}
