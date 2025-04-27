package com.numplates.nomera3.data.network.core;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * created by c7j on 24.05.18
 */
public class ResponseWrapper<T> implements Serializable {

    public static int CODE_400 = 400;

    @SerializedName("success")
    @Nullable
    private T data;

    @SerializedName("error")
    @Nullable
    private ResponseError err;

    @SerializedName("code")
    private int code;

    @SerializedName("message")
    @Nullable
    private String message;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public ResponseError getErr() {
        return err;
    }

    public void setErr(ResponseError err) {
        this.err = err;
    }

    public int getCode() { return this.code; }

    public void setCode(int code) { this.code = code; }

    public String getMessage() { return message; }

    public void setMessage(@Nullable String message) { this.message = message; }

    @Override
    public String toString() {
        return "ResponseWrapper{" +
            "data=" + data +
            ", err=" + err +
            ", code=" + code +
            ", message='" + message + '\'' +
            '}';
    }
}
