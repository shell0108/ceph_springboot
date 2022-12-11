package com.jhc.demo.controller.dto;

import lombok.Data;

//接收前端登录请求的参数
@Data
public class UserDTO {
    private String username;
    private String password;
    private Integer id;
    private String nickname;
    private String role;
    private String token;
}
