package com.bardiademon;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

import java.io.File;
import java.net.URL;

public final class Server extends AbstractVerticle
{

    private static final int PORT = 8888;

    @Override
    public void start(final Promise<Void> startPromise)
    {
        try
        {
            final HttpServer httpServer = vertx.createHttpServer();

            final Router router = Router.router(vertx);

            router.route("/").produces("text/plain").handler(this::homeHandler);

            final URL resource = getClass().getResource("/static");

            if (resource != null) router.route("/static/*").handler(StaticHandler.create(resource.getFile()));
            else throw new Exception("Cannot set static");

            httpServer.requestHandler(router).listen(PORT , "localhost" , result ->
            {
                if (result.succeeded())
                {
                    System.out.printf("Server running on port %d!\n" , PORT);
                    startPromise.complete();
                }
                else
                {
                    System.out.println("Error run server!");
                    startPromise.fail(result.cause());
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void homeHandler(final RoutingContext routingContext)
    {
        final HttpServerResponse response = routingContext.response();
        response.putHeader("content-type" , "text/plain");
        response.end("Server run by vert.x");
    }
}
