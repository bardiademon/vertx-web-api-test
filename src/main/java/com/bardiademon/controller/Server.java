package com.bardiademon.controller;

import com.bardiademon.data.DatabaseConnection;
import com.bardiademon.data.JdbcConnection;
import com.bardiademon.data.entity.Users;
import com.bardiademon.util.Path;
import io.smallrye.mutiny.Uni;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.reactive.mutiny.Mutiny;
import org.hibernate.reactive.provider.ReactiveServiceRegistryBuilder;
import org.hibernate.reactive.vertx.VertxInstance;

import javax.persistence.Persistence;
import java.net.URL;
import java.nio.file.FileSystemException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public final class Server extends AbstractVerticle implements VertxInstance
{
    private final Class<?>[] entities = new Class[]{
            Users.class
    };

    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final int PORT = 8888;

    private DatabaseConnection databaseConnection;

    private SessionFactory sessionFactory;

    @Override
    public void start(final Promise<Void> startPromise)
    {
        try
        {
            if (dbConnection())
            {
                initialQuery();

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
            else throw new Exception("Database connection error");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private boolean dbConnection()
    {
        return JdbcConnection.connect();
    }

    private void initialQuery()
    {
        final URL resource = getClass().getClassLoader().getResource(Path.RESOURCE_INITIAL_QUERY);
        if (resource != null)
        {
            try
            {
                final Buffer buffer = vertx.fileSystem().readFileBlocking(resource.getPath());

                if (buffer != null)
                {
                    if (JdbcConnection.isConnected())
                    {
                        final Connection connection = JdbcConnection.getJdbcConnection().getConnection();

                        try (final Statement statement = connection.createStatement())
                        {
                            final String initialQuery = buffer.toString();

                            statement.execute(initialQuery);
                            logger.info("The initial query was successfully completed!");
                        }
                        catch (SQLException e)
                        {
                            logger.error("Initial query, sql exception" , e);
                        }
                    }
                    else logger.error("Initial query, Database not connected!");
                }
                else throw new FileSystemException("buffer == null");
            }
            catch (FileSystemException e)
            {
                logger.error(String.format("Cannot read file: %s" , Path.RESOURCE_INITIAL_QUERY) , e);
            }
        }
        else logger.error(String.format("Resource is null: %s" , Path.RESOURCE_INITIAL_QUERY));
    }

    private void homeHandler(final RoutingContext routingContext)
    {
        final HttpServerResponse response = routingContext.response();
        response.putHeader("content-type" , "text/plain");
        response.end("Server run by vert.x");
    }

    private void hibernateConfig()
    {
        vertx.executeBlocking(e ->
        {
            final Configuration configuration = new Configuration().setProperties(getHibernateProperties());

            for (final Class<?> entity : entities) configuration.addAnnotatedClass(entity);

            final StandardServiceRegistryBuilder builder = new ReactiveServiceRegistryBuilder()
                    .addService(Server.class , this)
                    .applySettings(configuration.getProperties());

            final StandardServiceRegistry registry = builder.build();

            sessionFactory = configuration.buildSessionFactory(registry);

            if (!sessionFactory.isOpen()) throw new RuntimeException("Session is close!");


            Uni.createFrom().deferred(() ->
            {
                Persistence.createEntityManagerFactory("demo").unwrap(Mutiny.SessionFactory.class);

                return Uni.createFrom().voidItem();
            }).onItem().invoke(() -> logger.info("✅ Hibernate Reactive is ready"));
        });
    }

    private Properties getHibernateProperties()
    {
        final Properties properties = new Properties();
        properties.setProperty(Environment.DRIVER , "org.mysql.jdbc.DRIVER");
        properties.setProperty(Environment.URL , "jdbc:mysql://localhost:3306/vertx_web_api_test");
        properties.setProperty(Environment.USER , "root");
        properties.setProperty(Environment.PASS , "73487712");
//        properties.setProperty(Environment.DIALECT , "org.hibernate.dialect.MySQL55Dialect");
        properties.setProperty(Environment.HBM2DDL_CREATE_SCHEMAS , "true");
        properties.setProperty(Environment.HBM2DDL_DATABASE_ACTION , "create");
        properties.setProperty(Environment.SHOW_SQL , "true");
        properties.setProperty(Environment.POOL_SIZE , "10");
        return properties;
    }

    @Override
    public Vertx getVertx()
    {
        return vertx;
    }
}
