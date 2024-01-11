package com.csrd.pims.bean.web;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ResultWrapper<T> {
    public static final Integer ERROR_CODE = 1;
    public static final Integer SUCCESS_CODE = 0;
    /**
     * 响应状态码
     */
    private Integer code;
    /**
     * 响应信息
     */
    private String message;
    /**
     * 响应数据
     */
    private T data;


    protected ResultWrapper(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ResultWrapper<T> success() {
        return new ResultWrapper<>(SUCCESS_CODE, "success", null);
    }

    public static <T> ResultWrapper<T> success(Integer code, String message, T data) {
        return new ResultWrapper<>(code, message, data);
    }

    public static <T> ResultWrapper<T> success(T data) {
        return new ResultWrapper<>(SUCCESS_CODE, "success", data);
    }

    public static <T> ResultWrapper<T> error() {
        return new ResultWrapper<>(ERROR_CODE, "success", null);
    }

    public static <T> ResultWrapper<T> error(Integer code, String message, T data) {
        return new ResultWrapper<>(code, message, data);
    }

    public static <T> ResultWrapper<T> error(T data) {
        return new ResultWrapper<>(ERROR_CODE, "error", data);
    }

    public static <T> ResultWrapper<T> HuaweiOk(T data) {
        return new ResultWrapper<>(200, "ok", data);
    }
}
