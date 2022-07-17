package com.bardiademon.data.model;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public class User
{
    private long id;
    private String name;
    private String family;
    private String phone;
    private String username;
    private String password;
    private LocalDateTime createdAt;
}
