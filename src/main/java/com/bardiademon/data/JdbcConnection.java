package com.bardiademon.data;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class JdbcConnection
{
    private Connection connection;

    private static final Logger logger = LoggerFactory.getLogger(JdbcConnection.class);

    private static JdbcConnection jdbcConnection;

    public static boolean connect()
    {
        if (jdbcConnection == null) jdbcConnection = new JdbcConnection();

        final var URL_CONNECTION = String.format("jdbc:mysql://%s:%s/%s" ,
                ConnectionInfo.HOST.value , ConnectionInfo.PORT.value , ConnectionInfo.DATABASE_NAME.value);

        try
        {
            Class.forName("com.mysql.jdbc.Driver");

            jdbcConnection.connection = DriverManager.getConnection(URL_CONNECTION , ConnectionInfo.USERNAME.value , ConnectionInfo.PASSWORD.value);
            logger.info("Connected to database!");
            return true;
        }
        catch (SQLException | ClassNotFoundException e)
        {
            logger.error("Database connection error: " + e.getMessage());
            return false;
        }
    }

    private enum ConnectionInfo
    {
        HOST("localhost"),
        PORT("3306"),
        USERNAME("root"),
        PASSWORD("73487712"),
        DATABASE_NAME("vertx_web_api_test");
        private final String value;

        ConnectionInfo(final String value)
        {
            this.value = value;
        }
    }

    public Connection getConnection()
    {
        return connection;
    }

    public static boolean isConnected()
    {
        try
        {
            return (jdbcConnection != null && !jdbcConnection.getConnection().isClosed());
        }
        catch (SQLException e)
        {
            return false;
        }
    }

    public static JdbcConnection getJdbcConnection()
    {
        return jdbcConnection;
    }
}
