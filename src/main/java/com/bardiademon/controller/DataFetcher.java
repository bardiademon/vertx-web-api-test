package com.bardiademon.controller;

import com.bardiademon.data.dto.DtoLoginResult;
import com.bardiademon.data.dto.DtoUser;
import com.bardiademon.data.service.UserService;
import graphql.schema.DataFetchingEnvironment;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.jwt.JWTAuth;

import java.util.Map;

public record DataFetcher(Vertx vertx , JWTAuth jwtAuth)
{
    public Future<Map<String, Object>> user(final DataFetchingEnvironment environment)
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

    public Future<Map<String, Object>> bardiademon(final DataFetchingEnvironment environment)
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

    public Future<DtoLoginResult> login(final DataFetchingEnvironment environment)
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

    public Future<DtoUser> profile(final DataFetchingEnvironment environment)
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
