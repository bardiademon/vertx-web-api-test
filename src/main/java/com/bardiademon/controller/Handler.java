package com.bardiademon.controller;

import com.bardiademon.Main;
import com.bardiademon.util.Path;
import graphql.GraphQL;
import graphql.scalars.ExtendedScalars;
import graphql.schema.*;
import graphql.schema.idl.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;
import io.vertx.ext.web.handler.graphql.GraphQLHandlerOptions;
import io.vertx.ext.web.handler.graphql.schema.VertxDataFetcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

public sealed class Handler extends AbstractVerticle permits Server
{
    private JWTAuth jwtAuth;

    protected Handler()
    {

    }

    @Override
    public void start(Promise<Void> startPromise)
    {
        createJWTAuth();
    }

    private void createJWTAuth()
    {
        final URL resourcePrivateKey = Path.getResource(Path.RESOURCE_PRIVATE_KEY);
        final URL resourcePublicKey = Path.getResource(Path.RESOURCE_PUBLIC_KEY);

        if (resourcePrivateKey != null && resourcePublicKey != null)
        {
            jwtAuth = JWTAuth.create(vertx , new JWTAuthOptions()
                    .addPubSecKey(new PubSecKeyOptions()
                            .setAlgorithm("RS256")
                            .setBuffer(vertx.fileSystem().readFileBlocking(resourcePublicKey.getFile())))
                    .addPubSecKey(new PubSecKeyOptions()
                            .setAlgorithm("RS256")
                            .setBuffer(vertx.fileSystem().readFileBlocking(resourcePrivateKey.getFile()))
                    ));
        }
        else throw new RuntimeException("Not found public and private key");
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
        final DataFetcher dataFetcher = new DataFetcher(vertx , jwtAuth);

        final Method[] methods = dataFetcher.getClass().getMethods();

        for (final Method method : methods)
        {
            builder.dataFetcher(method.getName() , VertxDataFetcher.create(environment ->
            {
                Future<?> result;
                try
                {
                    result = (Future<?>) method.invoke(dataFetcher , environment);
                }
                catch (IllegalAccessException | InvocationTargetException e)
                {
                    throw new RuntimeException(e);
                }
                return result;
            }));
        }

        return builder;
    }


}
