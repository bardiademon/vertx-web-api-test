package com.bardiademon.data.dto;

import com.bardiademon.data.model.User;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Builder
@Getter
public class DtoUser
{
    private long id;
    private String name;
    private String family;
    private String phone;
    private String username;
    private OffsetDateTime createdAt;

    public static DtoUser getInstance(final User user)
    {
        return DtoUser.builder()
                .id(user.getId())
                .name(user.getName())
                .family(user.getFamily())
                .phone(user.getPhone())
                .username(user.getUsername())
                .createdAt(user.getCreatedAt().atOffset(ZoneOffset.ofHoursMinutes(4 , 55)))
                .build();
    }
}
