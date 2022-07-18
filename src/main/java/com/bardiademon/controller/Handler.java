package com.bardiademon.controller;

import com.bardiademon.Main;
import com.bardiademon.data.dto.DtoLoginResult;
import com.bardiademon.data.dto.DtoUser;
import com.bardiademon.data.service.UserService;
import com.bardiademon.util.Path;
import graphql.GraphQL;
import graphql.scalars.ExtendedScalars;
import graphql.schema.*;
import graphql.schema.idl.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
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
import java.time.LocalTime;
import java.util.Map;

public sealed class Handler extends AbstractVerticle permits Server
{
    private static final String[] GRAPHQL_QUERY_FIELDS_NAME = {
            "user" , "bardiademon" , "login" , "profile"
    };

    private JWTAuth jwtAuth;

    protected Handler()
    {

    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception
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

    public Future<DtoLoginResult> loginDataFetcher(final DataFetchingEnvironment environment)
    {
        return Future.future(event ->
        {
            final String username = environment.getArgument("username");
            final String password = environment.getArgument("password");

            UserService.getUserService(vertx).findByUsernamePassword(username , password , user ->
            {
                final String token = jwtAuth.generateToken(new JsonObject().put("user_id" , user.getId()) , new JWTOptions().setAlgorithm("RS256"));
                event.complete(DtoLoginResult.builder()
                        .token(token)
                        .result(token != null)
                        .user(DtoUser.getInstance(user))
                        .build());
            });
        });
    }

    public Future<DtoUser> profileDataFetcher(final DataFetchingEnvironment environment)
    {
        return Future.future(event -> jwtAuth.authenticate(new JsonObject().put("token" , environment.getArgument("token")))
                .onSuccess(user ->
                {
                    final long id = Long.parseLong(user.get("user_id").toString());
                    UserService.getUserService(vertx).findById(id , res -> event.complete(DtoUser.getInstance(res)));
                })
                .onFailure(eventFailure -> event.fail("Invalid token")));
    }

}
