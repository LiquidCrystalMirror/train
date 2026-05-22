package com.example.ticket.util;

import lombok.Data;

@Data
public class RespEntity {
    private int code;
    private String msg;
    private Object data;

    public RespEntity(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
}
