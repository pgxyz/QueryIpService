package com.hilton.queryservice.db;

import com.hilton.queryservice.core.QueryIpResponseEntity;
import com.mysql.cj.conf.PropertyKey;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@ExtendWith(DropwizardExtensionsSupport.class)
@DisabledForJreRange(min = JRE.JAVA_16)
public class QueryIpResponseDAOTest {

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

    @BeforeEach
    void setUp() {
        queryIpResponseDAO = new QueryIpResponseDAO(daoTestRule.getSessionFactory());
    }

    @Test
    public void createQueryIpResponse() {
        String queryIp = "111.11.1111";
        QueryIpResponseEntity queryIpResponseEntityObj = new QueryIpResponseEntity();
        queryIpResponseEntityObj.setStatus("success");
        queryIpResponseEntityObj.setQuery(queryIp);
        queryIpResponseEntityObj.setCity("Charlotte");
        queryIpResponseEntityObj.setCountry("US");
        queryIpResponseEntityObj.setAs("value");
        final QueryIpResponseEntity queryIpResponseEntity = daoTestRule.inTransaction(() ->
                queryIpResponseDAO.create(queryIpResponseEntityObj));

        assertThat(queryIpResponseEntity.getQuery().equals(queryIp));

        assertThat(queryIpResponseDAO.findByIp(queryIp)).isEqualTo(Optional.of(queryIpResponseEntity));
    }

    @Test
    public void findQueryIpWithInvalidQueryIp() {
        String queryIp1 = "111.11.1111";
        ;
        String queryIp2 = "112.12.1112";
        String invalidIp = "113.13.1113";
        QueryIpResponseEntity queryIpResponseEntityObj1 = new QueryIpResponseEntity();
        queryIpResponseEntityObj1.setStatus("success");
        queryIpResponseEntityObj1.setQuery(queryIp1);
        queryIpResponseEntityObj1.setCity("Charlotte");
        queryIpResponseEntityObj1.setCountry("US");
        queryIpResponseEntityObj1.setAs("value");

        QueryIpResponseEntity queryIpResponseEntityObj2 = new QueryIpResponseEntity();
        queryIpResponseEntityObj2.setStatus("success");
        queryIpResponseEntityObj2.setQuery(queryIp2);
        queryIpResponseEntityObj2.setCity("Charlotte");
        queryIpResponseEntityObj2.setCountry("US");
        queryIpResponseEntityObj2.setAs("value");
        final QueryIpResponseEntity queryIpResponseEntity1 = daoTestRule.inTransaction(() ->
                queryIpResponseDAO.create(queryIpResponseEntityObj1));

        final QueryIpResponseEntity queryIpResponseEntity2 = daoTestRule.inTransaction(() ->
                queryIpResponseDAO.create(queryIpResponseEntityObj1));

        assertThat(queryIpResponseDAO.findByIp(queryIp1)).isEqualTo(Optional.of(queryIpResponseEntity1));
        assertThat(queryIpResponseDAO.findByIp(invalidIp)).isEqualTo(Optional.empty());
    }
}
