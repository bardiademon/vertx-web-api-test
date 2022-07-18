package com.bardiademon.data.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DtoLoginResult
{
    private String token;
    private boolean result;
    private DtoUser user;
}
