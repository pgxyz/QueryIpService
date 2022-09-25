package com.hilton.queryservice.resources;

import com.hilton.queryservice.core.QueryIpResponseEntity;
import com.hilton.queryservice.db.QueryIpResponseDAO;
import com.mysql.cj.conf.PropertyKey;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.testing.junit5.DAOTestExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.MySQL57Dialect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Client;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers(disabledWithoutDocker = true)
@ExtendWith(DropwizardExtensionsSupport.class)
@DisabledForJreRange(min = JRE.JAVA_16)
public class QueryIPResourceIntegrationTest {

    @Container
    private static final MySQLContainer<?> MY_SQL_CONTAINER = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.24"));

    static {
        MY_SQL_CONTAINER.start();
    }

    public DAOTestExtension daoTestRule = DAOTestExtension.newBuilder()
            .customizeConfiguration(c -> c.setProperty(AvailableSettings.DIALECT, MySQL57Dialect.class.getName()))
            .setDriver(MY_SQL_CONTAINER.getDriverClassName())
            .setUrl(MY_SQL_CONTAINER.getJdbcUrl())
            .setUsername(MY_SQL_CONTAINER.getUsername())
            .setPassword(MY_SQL_CONTAINER.getPassword())
            .setProperty(PropertyKey.tlsVersions.getKeyName(), "TLSv1.1,TLSv1.2,TLSv1.3")
            .addEntityClass(QueryIpResponseEntity.class)
            .build();

    private QueryIpResponseDAO queryIpResponseDAO;
    private String serviceUrl = "http://ip-api.com/json/";
    private int expireCacheInSeconds = 5;
    private int maxCacheSize = 10;
    private Client client = null;
    private QueryIPResource underTest;

    @BeforeEach
    void setUp() {
        queryIpResponseDAO = new QueryIpResponseDAO(daoTestRule.getSessionFactory());
        client = new JerseyClientBuilder(new Environment("mockEnvironment")).build("RESTClient");
        underTest = new QueryIPResource(client, serviceUrl, expireCacheInSeconds, maxCacheSize, queryIpResponseDAO);
    }

    /**
     * Validate null ip
     */
    @Test
    public void testNullIPQuery() {
        BadRequestException thrown = assertThrows(
                BadRequestException.class,
                () -> underTest.queryIp(Optional.empty()),
                "Expected queryIp() to throw, but it didn't"
        );

        assertEquals(QueryIPResource.EMPTY_INPUT_EXCEPTION_MESSAGE, thrown.getMessage());
    }

    /**
     * Validate invalid IP
     *
     * @throws ExecutionException
     */
    @Test
    public void testInvalidIpQuery() throws ExecutionException {
        String ip = "invalidString";
        BadRequestException thrown = assertThrows(
                BadRequestException.class,
                () -> underTest.queryIp(Optional.of(ip)),
                "Expected queryIp() to throw, but it didn't"
        );
        assertEquals(QueryIPResource.INVALID_INPUT_EXCEPTION_MESSAGE, thrown.getMessage());
    }

    /**
     * Query Invalid IPV4 format IP
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testFetchForInvalidIPV4Format() throws ExecutionException, InterruptedException {
        String ip = "71.76.72";
        BadRequestException thrown = assertThrows(
                BadRequestException.class,
                () -> underTest.queryIp(Optional.of(ip)),
                "Expected queryIp() to throw, but it didn't"
        );
        assertEquals(QueryIPResource.INVALID_INPUT_EXCEPTION_MESSAGE, thrown.getMessage());
    }

    /**
     * Query IPV4 format IP
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testFetchForIPV4Format() throws ExecutionException, InterruptedException {
        String ip = "71.76.72.122";
        QueryIpResponseEntity response1 = underTest.queryIp(Optional.of(ip));
        assertEquals(ip, response1.getQuery());
        assertEquals("success", response1.getStatus());
        Thread.sleep(20);
        QueryIpResponseEntity response2 = underTest.queryIp(Optional.of(ip));
        assertEquals(ip, response2.getQuery());
        assertEquals("success", response2.getStatus());
    }

    /**
     * Query Invalid IPV6 format IP
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testFetchForInvalidIPV6Format() throws ExecutionException, InterruptedException {
        String ip = "2001:0db8:85a3:0000:0000:8a2e:0370:7334:9079:9097";
        BadRequestException thrown = assertThrows(
                BadRequestException.class,
                () -> underTest.queryIp(Optional.of(ip)),
                "Expected queryIp() to throw, but it didn't"
        );
        assertEquals(QueryIPResource.INVALID_INPUT_EXCEPTION_MESSAGE, thrown.getMessage());
    }

    /**
     * Query Valid IPV6 format IP
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testFetchForValidIPV6FormatIP() throws ExecutionException, InterruptedException {
        String ip = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
        QueryIpResponseEntity response1 = underTest.queryIp(Optional.of(ip));
        assertEquals("fail", response1.getStatus());
    }

    /**
     * Query Valid IPV6 format IP
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testFetchForValidPublicDomain() throws ExecutionException, InterruptedException {
        String ip = "google.com";
        QueryIpResponseEntity response1 = underTest.queryIp(Optional.of(ip));
        assertEquals("success", response1.getStatus());
    }

    /**
     * Query Invalid Public Domain
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testFetchForInvalidPublicDomain() throws ExecutionException, InterruptedException {
        String ip = "abc.d";
        BadRequestException thrown = assertThrows(
                BadRequestException.class,
                () -> underTest.queryIp(Optional.of(ip)),
                "Expected queryIp() to throw, but it didn't"
        );
        assertEquals(QueryIPResource.INVALID_INPUT_EXCEPTION_MESSAGE, thrown.getMessage());
    }

    /**
     * Test fetch from database
     *
     * @throws ExecutionException
     */
    @Test
    public void testFetchFromPersistedDatabase() throws ExecutionException {
        String ip = "111.11.111.11";
        QueryIpResponseEntity queryIpResponseEntityObj1 = new QueryIpResponseEntity();
        queryIpResponseEntityObj1.setStatus("success");
        queryIpResponseEntityObj1.setQuery(ip);
        queryIpResponseEntityObj1.setCity("Charlotte");
        queryIpResponseEntityObj1.setCountry("US");
        queryIpResponseEntityObj1.setAs("value");
        queryIpResponseEntityObj1.setPersisted("true");

        final QueryIpResponseEntity persistedEntity = daoTestRule.inTransaction(() ->
                queryIpResponseDAO.create(queryIpResponseEntityObj1));

        QueryIpResponseEntity response2 = underTest.queryIp(Optional.of(ip));
        //Fetch API call fetches the result from the database, with Persisted attribute is true
        assertEquals("true", response2.getPersisted());
        // Validate persisted entity is returned from the database lookup
        assertEquals(persistedEntity, response2);
    }

    /**
     * Test case to validate responses when Cache Size is maxed out
     *
     * @throws ExecutionException
     */
    @Test
    public void testWhenCacheSizeMaxedOut() throws ExecutionException {
        QueryIPResource cacheSizeMaxedOutResource = new QueryIPResource(client, serviceUrl, expireCacheInSeconds,
                1, queryIpResponseDAO);
        for (int i = 0; i < 5; i++) {
            String ip = "71.76.72.12" + i;
            QueryIpResponseEntity response = cacheSizeMaxedOutResource.queryIp(Optional.of(ip));
            assertEquals(ip, response.getQuery());
            assertEquals("success", response.getStatus());
        }

    }
}
