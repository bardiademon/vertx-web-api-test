package com.bardiademon.data;

import io.vertx.core.Vertx;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.*;

public final class DatabaseConnection
{
    private static final String HOST = "localhost";
    private static final int PORT = 3306;
    private static final String USERNAME = "root", PASSWORD = "73487712";
    private static final String DATABASE_NAME = "vertx_web_api_test";

    private final MySQLConnectOptions mySQLConnectOptions = new MySQLConnectOptions()
            .setHost(HOST)
            .setPort(PORT)
            .setUser(USERNAME)
            .setPassword(PASSWORD)
            .setDatabase(DATABASE_NAME);
    private final PoolOptions poolOptions = new PoolOptions()
            .setMaxSize(5);

    private SqlClient client;

    public boolean connect(Vertx vertx)
    {
        try
        {
            client = MySQLPool.client(vertx , mySQLConnectOptions , poolOptions);
            return true;
        }
        catch (IllegalStateException ignored)
        {
        }

        return false;
    }

    public SqlClient getSqlClient()
    {
        return client;
    }
}
