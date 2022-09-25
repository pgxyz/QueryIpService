package com.hilton.queryservice;

import com.hilton.queryservice.core.QueryIpResponseEntity;
import com.hilton.queryservice.db.QueryIpResponseDAO;
import com.hilton.queryservice.resources.QueryIPResource;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Query Service Main Application that initializes bundles in bootstrap and registers runs the query resource
 */
public class QueryIPServiceApplication extends Application<QueryIPServiceConfiguration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryIPServiceApplication.class);

    public static void main(final String[] args) throws Exception {
        new QueryIPServiceApplication().run(args);
    }

    private final HibernateBundle<QueryIPServiceConfiguration> hibernateBundle = new HibernateBundle<QueryIPServiceConfiguration>(QueryIpResponseEntity.class) {
        @Override
        public DataSourceFactory getDataSourceFactory(QueryIPServiceConfiguration configuration) {
            return configuration.getDataSourceFactory();
        }
    };

    @Override
    public String getName() {
        return "QueryIPService";
    }

    @Override
    public void initialize(final Bootstrap<QueryIPServiceConfiguration> bootstrap) {
        LOGGER.info("initializing hibernateBundle");
        bootstrap.addBundle(hibernateBundle);
        LOGGER.info("initializing Migrations Bundle");
        bootstrap.addBundle(new MigrationsBundle<QueryIPServiceConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(QueryIPServiceConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        });
    }

    @Override
    public void run(final QueryIPServiceConfiguration configuration, final Environment environment) {
        // Register QueryIPResource into the environment
        final QueryIpResponseDAO dao = new QueryIpResponseDAO(hibernateBundle.getSessionFactory());
        final Client client = new JerseyClientBuilder(environment).build("RESTClient");
        String ipServiceUrl = configuration.getIpServiceUrl();
        int expireCacheInSeconds = configuration.getExpireCacheInSeconds();
        int maxCacheSize = configuration.getMaxCacheSize();
        LOGGER.info("registering QueryIPResource in environment jersey");
        environment.jersey().register(new QueryIPResource(client, ipServiceUrl, expireCacheInSeconds, maxCacheSize, dao));

        OpenAPI openAPI = new OpenAPI();
        Info info = new Info().title("Query Geolocation by an IP API").description("API to query Geolocation by an IP").contact(new Contact().email("pradeep.gummi@gmail.com"));

        openAPI.info(info);
        SwaggerConfiguration swaggerConfiguration = new SwaggerConfiguration().openAPI(openAPI).prettyPrint(true).resourcePackages(Stream.of("com.hilton.queryservice.resources").collect(Collectors.toSet()));

        environment.jersey().register(new OpenApiResource().openApiConfiguration(swaggerConfiguration));
    }

}
