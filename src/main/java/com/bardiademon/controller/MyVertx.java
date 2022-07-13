package com.bardiademon.controller;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import org.hibernate.reactive.vertx.VertxInstance;

public class MyVertx extends AbstractVerticle implements VertxInstance
{

    @Override
    public Vertx getVertx()
    {
        return vertx;
    }
}
