package com.bardiademon;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

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

            router.get("/").handler(this::homeHandler);

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
