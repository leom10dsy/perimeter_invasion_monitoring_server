package com.csrd.pims.bean.web;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResultWrapperEx<T> extends ResultWrapper<T> {

    private String status;

    private static final Integer ERROR_CODE = 1;
    private static final Integer SUCCESS_CODE = 0;
    private static final String SUCCESS = "1";
    private static final String FAIL = "0";

    protected ResultWrapperEx(Integer code, String message, T data) {
        super(code, message, data);
    }

    public ResultWrapperEx(Integer code, String status, String message, T data) {
        super(code, message, data);
        this.status = status;
    }

    public static <T> ResultWrapperEx<T> success() {
        return new ResultWrapperEx<T>(SUCCESS_CODE, SUCCESS, "", null);
    }

    public static <T> ResultWrapperEx<T> success(T data) {
        return new ResultWrapperEx<T>(SUCCESS_CODE, SUCCESS, "", data);
    }

    public static <T> ResultWrapperEx<T> success(Integer code, String message, T data) {
        return new ResultWrapperEx<T>(SUCCESS_CODE, SUCCESS, message, data);
    }


    public static <T> ResultWrapperEx<T> error() {
        return new ResultWrapperEx<T>(ERROR_CODE, FAIL, "", null);
    }

    public static <T> ResultWrapperEx<T> error(T data) {
        return new ResultWrapperEx<T>(ERROR_CODE, FAIL, "", data);
    }


}
