package com.bardiademon.data.service;

import com.bardiademon.data.DBConnection.JDBCPoolConnection;
import com.bardiademon.data.DBConnection.JdbcConnection;
import com.bardiademon.data.model.User;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class UserService extends Service
{

    private UserService(final Vertx vertx)
    {
        super(vertx);
    }

    private static UserService userService;

    public static UserService getUserService(final Vertx vertx)
    {
        if (userService == null) userService = new UserService(vertx);
        return userService;
    }

    public void findByUsernamePassword(final String username , final String password , final Handler<User> handler)
    {
        final JDBCPoolConnection jdbcPoolConnection = connectJDBCPool(vertx);
        if (JdbcConnection.isConnected())
        {
            jdbcPoolConnection.getJdbcPool().preparedQuery(querySelectUserByUsernamePassword())
                    .execute(Tuple.of(username , getPasswordToHash(password , username)))
                    .onFailure(event ->
                    {
                        System.out.println(event.getMessage());
                        handler.handle(null);
                    }).onSuccess(rows ->
                    {
                        if (rows.size() > 0)
                        {
                            final Row row = rows.iterator().next();
                            handler.handle(User.rowToUser(row));
                        }
                        else handler.handle(null);
                    });
        }
        else handler.handle(null);
    }

    public void findById(final long id , final Handler<User> handler)
    {
        final JDBCPoolConnection jdbcPoolConnection = connectJDBCPool(vertx);
        if (JdbcConnection.isConnected())
        {
            jdbcPoolConnection.getJdbcPool().preparedQuery(querySelectUserById())
                    .execute(Tuple.of(id))
                    .onFailure(event ->
                    {
                        System.out.println(event.getMessage());
                        handler.handle(null);
                    }).onSuccess(rows ->
                    {
                        if (rows.size() > 0)
                        {
                            final Row row = rows.iterator().next();
                            handler.handle(User.rowToUser(row));
                        }
                        else handler.handle(null);
                    });
        }
        else handler.handle(null);
    }

    /**
     * for JDBC connection => java.sql
     */
    public User getUserByUsernamePassword(final String username , final String password)
    {
        if (connectJdbc())
        {
            final JdbcConnection jdbcConnection = JdbcConnection.getJdbcConnection();

            try (final PreparedStatement statement = jdbcConnection.getConnection().prepareStatement(querySelectUserByUsernamePassword()))
            {
                statement.setString(1 , username);
                statement.setString(2 , getPasswordToHash(password , username));
                try (final ResultSet resultSet = statement.executeQuery();)
                {
                    if (resultSet.first() && resultSet.next()) return User.resultSetToUser(resultSet);
                }
            }
            catch (SQLException e)
            {
                throw new RuntimeException(e);
            }
        }

        return null;
    }

    public String getPasswordToHash(final String passwordToHash , final Object salt)
    {
        try
        {
            final MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(salt.toString().getBytes(StandardCharsets.UTF_8));
            final byte[] bytes = md.digest(passwordToHash.getBytes(StandardCharsets.UTF_8));
            final StringBuilder sb = new StringBuilder();
            for (final byte aByte : bytes) sb.append(Integer.toString((aByte & 0xff) + 0x100 , 16).substring(1));
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private String querySelectUserByUsernamePassword()
    {
        return "select * from `users` where `username` = ? and `password` = ?";
    }

    private String querySelectUserById()
    {
        return "select * from `users` where `id` = ?";
    }
}
