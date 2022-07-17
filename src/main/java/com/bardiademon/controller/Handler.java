package com.bardiademon.controller;

import com.bardiademon.Main;
import graphql.GraphQL;
import graphql.scalars.ExtendedScalars;
import graphql.schema.*;
import graphql.schema.idl.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.impl.future.FailedFuture;
import io.vertx.core.impl.future.SucceededFuture;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;
import io.vertx.ext.web.handler.graphql.GraphQLHandlerOptions;
import io.vertx.ext.web.handler.graphql.schema.VertxDataFetcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;

public sealed class Handler extends AbstractVerticle permits Server
{
    private static final String[] GRAPHQL_QUERY_FIELDS_NAME = {
            "user" , "bardiademon" , "login"
    };

    protected Handler()
    {
    }

    protected void homeHandler(final RoutingContext routingContext)
    {
        final HttpServerResponse response = routingContext.response();
        response.putHeader("content-type" , "text/plain");
        response.end("Server run by vert.x");
    }

    protected void loginHandler(final RoutingContext context)
    {
        final HttpServerResponse response = context.response();
        response.putHeader("content-type" , "application/json");
    }

    protected void graphqlHandler(final RoutingContext context)
    {
        GraphQLHandlerOptions options = new GraphQLHandlerOptions()
                // enable multipart for file upload.
                .setRequestMultipartEnabled(true)
                .setRequestBatchingEnabled(true);

        GraphQLHandler.create(setupGraphQL() , options).handle(context);
    }

    protected GraphQL setupGraphQL()
    {
        final URL resource = Main.class.getClassLoader().getResource("graphql/schema.graphql");
        if (resource == null) return null;

        final String schema = vertx.fileSystem().readFileBlocking(resource.getFile()).toString();

        final SchemaParser schemaParser = new SchemaParser();

        final TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

        final RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
                .scalar(ExtendedScalars.DateTime)
                .scalar(ExtendedScalars.GraphQLLong)
                .type("Query" , this::graphQlQueryDataFetch).build();

        final SchemaGenerator schemaGenerator = new SchemaGenerator();
        final GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry , runtimeWiring);

        return GraphQL.newGraphQL(graphQLSchema).build();
    }

    private TypeRuntimeWiring.Builder graphQlQueryDataFetch(final TypeRuntimeWiring.Builder builder)
    {
        for (final String fieldName : GRAPHQL_QUERY_FIELDS_NAME)
        {
            try
            {
                final Method method = Handler.this.getClass().getMethod(String.format("%sDataFetcher" , fieldName) , DataFetchingEnvironment.class);
                builder.dataFetcher(fieldName , VertxDataFetcher.create(environment ->
                {
                    Future<?> result;
                    try
                    {
                        result = (Future<?>) method.invoke(Handler.this , environment);
                    }
                    catch (IllegalAccessException | InvocationTargetException e)
                    {
                        throw new RuntimeException(e);
                    }
                    return result;
                }));
            }
            catch (NoSuchMethodException e)
            {
                throw new RuntimeException(e);
            }
        }

        return builder;
    }

    public Future<Map<String, Object>> userDataFetcher(final DataFetchingEnvironment environment)
    {
        return Future.future(event ->
        {
            final JsonObject res = new JsonObject();

            res.put("id" , 1);
            res.put("name" , "bardia");
            res.put("family" , "demon");
            res.put("phone" , "989170221393");

            event.complete(res.getMap());
        });
    }

    public Future<Map<String, Object>> bardiademonDataFetcher(final DataFetchingEnvironment environment)
    {
        return Future.future(event ->
        {
            final JsonObject res = new JsonObject();
            res.put("id" , "@bardiademon");
            res.put("name" , "bardia");
            res.put("email" , "bardiademon@gmail.com");

            event.complete(res.getMap());
        });
    }

    public Future<Map<String, Object>> loginDataFetcher(final DataFetchingEnvironment environment)
    {
        Future.future(event ->
        {
            final String username = environment.getArgument("username");
            final String password = environment.getArgument("password");

            final JsonObject res = new JsonObject();
            res.put("result" , false);
            res.put("token" , "IS TOKEN");
            event.complete(res.getMap());
        });
    }

}
