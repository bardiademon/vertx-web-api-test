package com.bardiademon.controller;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public sealed class Handler extends AbstractVerticle permits Server
{
    protected Handler()
    {
    }

    protected void homeHandler(final RoutingContext routingContext)
    {
        final HttpServerResponse response = routingContext.response();
        response.putHeader("content-type" , "text/plain");
        response.end("Server run by vert.x");
    }

}
