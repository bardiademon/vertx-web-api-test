package com.bardiademon.data.model;

import com.bardiademon.Main;
import io.vertx.sqlclient.Row;
import lombok.Builder;
import lombok.Getter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder
@Getter
public class User
{
    private long id;
    private String name;
    private String family;
    private String phone;
    private String username;
    private String password;
    private LocalDateTime createdAt;


    public static User resultSetToUser(final ResultSet resultSet) throws SQLException
    {
        return User.builder()
                .id(resultSet.getLong("id"))
                .name(resultSet.getString("name"))
                .family(resultSet.getString("family"))
                .phone(resultSet.getString("phone"))
                .username(resultSet.getString("username"))
                .createdAt(resultSet.getTimestamp("created_at").toLocalDateTime())
                .build();
    }

    public static User rowToUser(final Row row)
    {
        return User.builder()
                .id(row.getLong("id"))
                .name(row.getString("name"))
                .family(row.getString("family"))
                .phone(row.getString("phone"))
                .username(row.getString("username"))
                .createdAt(row.getLocalDateTime("created_at"))
                .build();
    }

    public Map<String, Object> toMap()
    {
        final Map<String, Object> map = new HashMap<>();
        map.put("id" , id);
        map.put("name" , name);
        map.put("family" , family);
        map.put("phone" , phone);
        map.put("username" , username);
        map.put("created_at" , createdAt);

        return map;
    }
}
