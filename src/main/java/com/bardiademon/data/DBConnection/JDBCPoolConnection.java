package com.bardiademon.data.DBConnection;

import io.vertx.core.Vertx;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.PoolOptions;

public class JDBCPoolConnection
{

    private static JDBCPoolConnection jdbcPoolConnection;

    private JDBCPool jdbcPool;

    public static JDBCPoolConnection connect(final Vertx vertx)
    {
        if (jdbcPoolConnection == null) jdbcPoolConnection = new JDBCPoolConnection();

        if (!isConnect())
        {
            final var URL_CONNECTION = String.format("jdbc:mysql://%s:%s/%s" ,
                    ConnectionInfo.HOST.value , ConnectionInfo.PORT.value , ConnectionInfo.DATABASE_NAME.value);

            jdbcPoolConnection.jdbcPool = JDBCPool.pool(vertx , new JDBCConnectOptions()
                            .setJdbcUrl(URL_CONNECTION)
                            .setUser(ConnectionInfo.USERNAME.value)
                            .setPassword(ConnectionInfo.PASSWORD.value) ,
                    new PoolOptions().setMaxSize(16)
            );
        }

        return jdbcPoolConnection;
    }

    public static boolean isConnect()
    {
        return jdbcPoolConnection != null && jdbcPoolConnection.jdbcPool != null;
    }

    public JDBCPool getJdbcPool()
    {
        return jdbcPool;
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
}
