package com.bardiademon.controller;

import com.bardiademon.data.DatabaseConnection;
import com.bardiademon.data.entity.Users;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniAwait;
import io.smallrye.mutiny.groups.UniOnItem;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
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
import java.util.Properties;

public final class Server extends AbstractVerticle implements VertxInstance
{
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private Mutiny.SessionFactory emf;  // (1)
    private static final int PORT = 8888;

    private final DatabaseConnection databaseConnection = new DatabaseConnection();

    @Override
    public void start(final Promise<Void> startPromise)
    {
        try
        {
            hibernateConfig();

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

    private void hibernateConfig()
    {
        executeBlocking();

        final Configuration configuration = new Configuration().setProperties(getHibernateProperties());

        configuration.addAnnotatedClass(Users.class);

        final StandardServiceRegistryBuilder builder = new ReactiveServiceRegistryBuilder()
                .addService(Server.class , this)
                .applySettings(configuration.getProperties());

        final StandardServiceRegistry registry = builder.build();

        SessionFactory sessionFactory = configuration.buildSessionFactory(registry);

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
        properties.setProperty(Environment.HBM2DDL_AUTO , "update");
        properties.setProperty(Environment.FORMAT_SQL , "true");
        properties.setProperty(Environment.POOL_SIZE , "10");
        return properties;
    }

    private void executeBlocking()
    {
        final Uni<Void> startHibernate = Uni.createFrom().deferred(() ->
        {
            emf = Persistence
                    .createEntityManagerFactory("demo")
                    .unwrap(Mutiny.SessionFactory.class);

            return Uni.createFrom().voidItem();
        });

        vertx.executeBlocking(e ->
        {
            System.out.println("executeBlocking");
            final UniOnItem<Void> voidUniOnItem = startHibernate.onItem();
            voidUniOnItem.invoke(() -> logger.info("✅ Hibernate Reactive is ready"));
        });
    }

    @Override
    public Vertx getVertx()
    {
        return vertx;
    }
}
