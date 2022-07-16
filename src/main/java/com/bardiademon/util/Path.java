package com.bardiademon.util;

import java.net.URL;

public final class Path
{
    public static final String ROOT = System.getProperty("user.dir");

    public static final String RESOURCE_INITIAL_QUERY = "initial_query.sql";

    public URL getResource(final String path)
    {
        return getClass().getClassLoader().getResource(path);
    }

    private Path()
    {
    }
}
