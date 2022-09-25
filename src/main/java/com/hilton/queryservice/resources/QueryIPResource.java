package com.hilton.queryservice.resources;

import com.hilton.queryservice.core.QueryIpResponseEntity;
import com.hilton.queryservice.db.QueryIpResponseDAO;
import io.dropwizard.hibernate.UnitOfWork;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.glassfish.jersey.internal.guava.CacheBuilder;
import org.glassfish.jersey.internal.guava.CacheLoader;
import org.glassfish.jersey.internal.guava.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


/**
 * Query IP Resource to fetch the IP Information and executes in the following steps on ip query:
 * 1. Fetch from remote client call
 * 2. Store query response in database
 * 3. Cache the result with in-memory cache for cache expiry time configured as expireCacheInSeconds
 * 4. Further calls are queried from cache, then fallback to database, and remote api call
 */
@Path("/geolocation")
@Produces(MediaType.APPLICATION_JSON)
public class QueryIPResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryIPResource.class);

    public static final String PUBLIC_DOMAIN_FORMAT = "^([a-z0-9]+(-[a-z0-9]+)*\\.)+[a-z]{2,}$";
    public static String EMPTY_INPUT_EXCEPTION_MESSAGE = "queryIp in the path /geolocation/ip/{queryIp} is required field to search for ip";
    public static String INVALID_INPUT_EXCEPTION_MESSAGE = "queryIp in the path /geolocation/ip/{queryIp} is not in valid IPv4 or IPv6 format";
    private final Client client;

    // remote client service url
    private final String serviceUrl;

    // cache expiry time in seconds
    private final int expireCacheInSeconds;

    // max cache size. this is currently in memory and needs to be moved to external cache such as redis etc
    private final int maxCacheSize;

    // in-memory cache
    private final LoadingCache<String, QueryIpResponseEntity> ipCache;

    // DAO for query response persistence and lookup
    private final QueryIpResponseDAO queryIpResponseDAO;

    public QueryIPResource(Client client, String serviceUrl, int expireCacheInSeconds, int maxCacheSize, QueryIpResponseDAO queryIpResponseDAO) {
        this.client = client;
        this.serviceUrl = serviceUrl;
        this.expireCacheInSeconds = expireCacheInSeconds;
        this.maxCacheSize = maxCacheSize;
        this.queryIpResponseDAO = queryIpResponseDAO;

        /**
         * Initialize Cache with load from remote api call and persist in db table
         */
        ipCache = CacheBuilder.newBuilder().maximumSize(maxCacheSize).expireAfterAccess(this.expireCacheInSeconds, TimeUnit.SECONDS).build(new CacheLoader<String, QueryIpResponseEntity>() {
            public QueryIpResponseEntity load(String key) throws Exception {
                LOGGER.info("Loading key " + key + " from Database as not in cache");
                QueryIpResponseEntity queryIpResponseEntity = fetchQueryIpFromDatabase(key);
                if (queryIpResponseEntity == null) {
                    LOGGER.info("IP " + key + " is not present in database, fetch from remote api call");
                    queryIpResponseEntity = fetchQueryIpFromRemoteCall(Optional.of(key));
                    queryIpResponseEntity.setPersisted("true");
                    queryIpResponseDAO.create(queryIpResponseEntity);
                }
                return queryIpResponseEntity;
            }
        });
    }

    /**
     * Fetch query IP Rest API Call
     * Request format /geolocation/ip/{queryIp}
     *
     * @param ipOptional ip String
     * @return query ip response entity
     * @throws BadRequestException if null
     * @throws ExecutionException
     */
    @GET
    @Path(("/ip/{queryIp}"))
    @Operation(description = "Fetch Geo Location by an IP")
    @UnitOfWork
    public QueryIpResponseEntity queryIp(@PathParam("queryIp") Optional<String> ipOptional) throws BadRequestException, ExecutionException {

        if (ipOptional == null || ipOptional.isEmpty()) {
            LOGGER.error(" empty query ip returning error message: " + EMPTY_INPUT_EXCEPTION_MESSAGE);
            throw new BadRequestException(EMPTY_INPUT_EXCEPTION_MESSAGE);
        }
        String ipString = ipOptional.get();
        InetAddressValidator inetAddressValidator = InetAddressValidator.getInstance();
        if (!inetAddressValidator.isValidInet4Address(ipString) && !inetAddressValidator.isValidInet6Address(ipString) && !ipString.matches(PUBLIC_DOMAIN_FORMAT)) {
            LOGGER.error(" invalid ip input message: " + INVALID_INPUT_EXCEPTION_MESSAGE);
            throw new BadRequestException(INVALID_INPUT_EXCEPTION_MESSAGE);
        }
        return ipCache.get(ipString);
    }

    /**
     * Remote client call to fetch IP information by remote api call
     *
     * @param ip address
     * @return ip response entity
     */
    private QueryIpResponseEntity fetchQueryIpFromRemoteCall(Optional<String> ip) {
        String targetUrl = null;
        if (ip.isPresent() && ip.get() != "") {
            targetUrl = serviceUrl + ip.get();
        } else {
            targetUrl = serviceUrl;
        }
        WebTarget webTarget = client.target(targetUrl);
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.get();
        QueryIpResponseEntity queryIpResponseEntity = response.readEntity(QueryIpResponseEntity.class);
        LOGGER.info(" successfully fetched the ip:" + ip + " from remote api service " + targetUrl + " and query status is :" + queryIpResponseEntity.getStatus());
        return queryIpResponseEntity;
    }

    /**
     * Fetch IP Query response persisted in the database as a fallback mechanism from cache lookup
     *
     * @param ipString ip string
     * @return query ip response persisted in database
     */
    private QueryIpResponseEntity fetchQueryIpFromDatabase(String ipString) {
        Optional<QueryIpResponseEntity> optionalQueryIpResponseEntity = queryIpResponseDAO.findByIp(ipString);
        if (optionalQueryIpResponseEntity != null && optionalQueryIpResponseEntity.isPresent()) {
            LOGGER.info(" successfully queried ip : " + ipString + " from database ");
            return optionalQueryIpResponseEntity.get();
        }
        return null;
    }
}
