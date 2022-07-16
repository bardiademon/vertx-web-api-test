package com.bardiademon.controller;

import com.bardiademon.Main;
import graphql.GraphQL;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;
import io.vertx.ext.web.handler.graphql.GraphQLHandlerOptions;

import java.net.URL;
import java.util.Map;

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
        final URL resource = Main.class.getClassLoader().getResource("graphql/schema.gql");
        if (resource == null) return null;

        final String schema = vertx.fileSystem().readFileBlocking(resource.getFile()).toString();

        final SchemaParser schemaParser = new SchemaParser();
        final TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);


        final RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring().type("Query" , this::graphQlQueryDataFetch).build();

        final SchemaGenerator schemaGenerator = new SchemaGenerator();
        final GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry , runtimeWiring);

        return GraphQL.newGraphQL(graphQLSchema).build();
    }

    private TypeRuntimeWiring.Builder graphQlQueryDataFetch(final TypeRuntimeWiring.Builder builder)
    {
        return builder.dataFetcher("user" , this::usersDataFetcher)
                .dataFetcher("bardiademon" , this::bardiademonDataFetcher);
    }

    private Map<String, Object> usersDataFetcher(final DataFetchingEnvironment environment)
    {
        final JsonObject res = new JsonObject();
        res.put("id" , 1);
        res.put("name" , "bardia");
        res.put("family" , "demon");
        res.put("phone" , "989170221393");
        return res.getMap();
    }

    private Map<String, Object> bardiademonDataFetcher(final DataFetchingEnvironment environment)
    {
        final JsonObject res = new JsonObject();
        res.put("id" , "@bardiademon");
        res.put("name" , "bardia");
        res.put("email" , "bardiademon@gmail.com");
        return res.getMap();
    }

}
