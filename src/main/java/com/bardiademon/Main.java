package com.bardiademon;

import com.bardiademon.controller.Server;
import io.vertx.core.Vertx;

import java.net.URL;

public final class Main
{
    public static void main(final String[] args)
    {
        final Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new Server());
    }
}