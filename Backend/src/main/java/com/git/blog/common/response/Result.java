package com.git.blog.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result implements Serializable {

    public static final String RESULT_OK = "ok";

    public static final String RESULT_NOT_OK = "not_ok";

    private String code;

    private String message;

    private Object data;

    public static Result success() {
        return new Result(RESULT_OK, null, null);
    }

    public static Result success(Object data) {
        return new Result(RESULT_OK, "操作成功", data);
    }

    public static Result success(String message, Object data) {
        return new Result(RESULT_OK, message, data);
    }

    public static Result fail() {
        return new Result(RESULT_NOT_OK, null, null);
    }

    public static Result fail(String message) {
        return new Result(RESULT_NOT_OK, message, null);
    }

    public static Result fail(String message, Object data) {
        return new Result(RESULT_NOT_OK, message, data);
    }
}
