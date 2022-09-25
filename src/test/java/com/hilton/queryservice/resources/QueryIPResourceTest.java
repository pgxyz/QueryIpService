package com.hilton.queryservice.resources;

import com.hilton.queryservice.core.QueryIpResponseEntity;
import com.hilton.queryservice.db.QueryIpResponseDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class QueryIPResourceTest {

    QueryIPResource underTest;
    private QueryIpResponseDAO queryIpResponseDAO;
    private String serviceUrl = "testUrl";
    private int expireCacheInSeconds = 10;
    private int maxCacheSize = 100;
    private Client client;

    @BeforeEach
    void setUp() {
        queryIpResponseDAO = Mockito.mock(QueryIpResponseDAO.class);
        client = Mockito.mock(Client.class);
        underTest = new QueryIPResource(client, serviceUrl, expireCacheInSeconds, maxCacheSize, queryIpResponseDAO);
    }

    @Test
    public void testNullQueryIp() {
        BadRequestException thrown = assertThrows(
                BadRequestException.class,
                () -> underTest.queryIp(Optional.empty()),
                "Expected queryIp() to throw, but it didn't"
        );
        assertEquals(QueryIPResource.EMPTY_INPUT_EXCEPTION_MESSAGE, thrown.getMessage());
    }

    @Test
    public void testInvalidQueryIpFormat() {
        BadRequestException thrown = assertThrows(
                BadRequestException.class,
                () -> underTest.queryIp(Optional.of("invalidString")),
                "Expected queryIp() to throw, but it didn't"
        );
        assertEquals(QueryIPResource.INVALID_INPUT_EXCEPTION_MESSAGE, thrown.getMessage());
    }

    @Test
    public void testFetchFromCache() throws InterruptedException, ExecutionException {
        String ip = "111.11.111.111";
        Optional<String> queryIp = Optional.of(ip);
        QueryIpResponseEntity expectedResponse = new QueryIpResponseEntity();
        expectedResponse.setQuery(ip);
        expectedResponse.setPersisted("true");
        //Thread.sleep(expireCacheInSeconds);
        when(queryIpResponseDAO.findByIp(ip)).thenReturn(Optional.of(expectedResponse));
        QueryIpResponseEntity result = underTest.queryIp(queryIp);
        assertEquals(ip, result.getQuery());
        assertEquals("true", result.getPersisted());
    }

    @Test
    public void testFetchFromDatabaseAfterExpiredCache() throws InterruptedException, ExecutionException {
        String ip = "111.11.111.11";
        Optional<String> queryIp = Optional.of(ip);
        QueryIpResponseEntity expectedResponse = new QueryIpResponseEntity();
        expectedResponse.setQuery(ip);
        expectedResponse.setPersisted("true");
        Thread.sleep(expireCacheInSeconds);
        when(queryIpResponseDAO.findByIp(ip)).thenReturn(Optional.of(expectedResponse));
        QueryIpResponseEntity result = underTest.queryIp(queryIp);
        assertEquals(ip, result.getQuery());
        assertEquals("true", result.getPersisted());
    }

    @Test
    public void testFetchFromRemoteApiCallAndLaterPersistedInDB() throws ExecutionException {
        String ip = "111.11.111.11";
        Optional<String> queryIp = Optional.of(ip);
        WebTarget webTarget = Mockito.mock(WebTarget.class);
        Invocation.Builder invocationBuilder = Mockito.mock(Invocation.Builder.class);
        Response response = Mockito.mock(Response.class);

        QueryIpResponseEntity expectedResponse = new QueryIpResponseEntity();
        expectedResponse.setQuery(ip);
        expectedResponse.setCity("Charlotte");
        when(queryIpResponseDAO.findByIp(ip)).thenReturn(Optional.empty());
        when(client.target(anyString())).thenReturn(webTarget);
        when(webTarget.request(anyString())).thenReturn(invocationBuilder);
        when(invocationBuilder.get()).thenReturn(response);
        when(response.readEntity(QueryIpResponseEntity.class)).thenReturn(expectedResponse);
        QueryIpResponseEntity result = underTest.queryIp(queryIp);
        verify(queryIpResponseDAO).create(any());
        assertEquals(ip, result.getQuery());
        assertNull(null, result.getPersisted());
        assertEquals(expectedResponse, result);
    }

}
