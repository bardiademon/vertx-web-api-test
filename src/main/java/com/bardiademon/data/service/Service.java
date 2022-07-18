package com.bardiademon.data.service;

import com.bardiademon.data.DBConnection.JDBCPoolConnection;
import com.bardiademon.data.DBConnection.JdbcConnection;
import io.vertx.core.Vertx;
import org.jetbrains.annotations.NotNull;

public abstract class Service
{

    protected final Vertx vertx;

    protected Service(final Vertx vertx)
    {
        this.vertx = vertx;
    }

    protected boolean connectJdbc()
    {
        return JdbcConnection.connect();
    }

    protected @NotNull JDBCPoolConnection connectJDBCPool(final Vertx vertx)
    {
        return JDBCPoolConnection.connect(vertx);
    }
}
