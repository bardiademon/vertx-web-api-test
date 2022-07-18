package com.bardiademon.util;

import com.bardiademon.Main;
import com.bardiademon.data.service.UserService;

import java.net.URL;

public final class Path
{
    public static final String ROOT = System.getProperty("user.dir");

    public static final String RESOURCE_INITIAL_QUERY = "initial_query.sql";
    public static final String RESOURCE_PRIVATE_KEY = "pem/private_key.pem";
    public static final String RESOURCE_PUBLIC_KEY = "pem/public.pem";

    public static URL getResource(final String path)
    {
        return Main.class.getClassLoader().getResource(path);
    }

    private Path()
    {
    }
}
