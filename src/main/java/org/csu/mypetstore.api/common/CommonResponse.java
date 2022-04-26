package org.csu.mypetstore.api.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.io.Serializable;

@Getter
// 空值属性不加入返回的JSON当中
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse<T> implements Serializable {

    private int status;
    private String msg;
    private T data;

    private CommonResponse(int status, T data) {
        this.status = status;
        this.data = data;
    }

    private CommonResponse(int status, String msg, T data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    private CommonResponse(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    private CommonResponse(int status) {
        this.status = status;
    }

    @JsonIgnore
    public boolean isSuccess(){
        return this.status == ResponseCode.SUCCESS.getCode();
    }

    public T getData() {
        return data;
    }

    public static <T> CommonResponse<T> createForSuccess(){
        return new CommonResponse<T>(ResponseCode.SUCCESS.getCode());
    }

    public static <T> CommonResponse<T> createForSuccess(T data){
        return new CommonResponse<T>(ResponseCode.SUCCESS.getCode(), data);
    }

    public static <T> CommonResponse<T> createForSuccessMessage(String msg){
        return new CommonResponse<T>(ResponseCode.SUCCESS.getCode(), msg);
    }

    public static <T> CommonResponse<T> createForSuccessMessage(String msg, T data){
        return new CommonResponse<T>(ResponseCode.SUCCESS.getCode(), msg, data);
    }

    public static <T> CommonResponse<T> createForError(){
        return new CommonResponse<T>(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getDescription());
    }

    public static <T> CommonResponse<T> createForError(String msg){
        return new CommonResponse<T>(ResponseCode.ERROR.getCode(), msg);
    }

    public static <T> CommonResponse<T> createForError(int code, String msg){
        return new CommonResponse<T>(code, msg);
    }

    public static <T> CommonResponse<T> createForNeedLogin(String msg){
        return new CommonResponse<T>(ResponseCode.NEED_LOGIN.getCode(), msg);
    }

    public static <T> CommonResponse<T> createForArgument(String msg){
        return new CommonResponse<T>(ResponseCode.ILLEGAL_ARGUMENT.getCode(), msg);
    }


}
