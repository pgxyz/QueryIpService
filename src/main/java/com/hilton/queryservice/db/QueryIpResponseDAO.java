package com.hilton.queryservice.db;

import com.hilton.queryservice.core.QueryIpResponseEntity;
import io.dropwizard.hibernate.AbstractDAO;
//import jakarta.persistence.Query;
import org.hibernate.SessionFactory;

import javax.persistence.Query;
import java.util.List;
import java.util.Optional;

/**
 * DAO for QueryIpResponseEntity persisted in database
 */
public class QueryIpResponseDAO extends AbstractDAO<QueryIpResponseEntity> {

    public QueryIpResponseDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    /**
     * Find entity by ip
     * @param ip string
     * @return Optional of Query Ip Response entity
     */
    public Optional<QueryIpResponseEntity> findByIp(String ip) {
        Query namedQuery = currentSession().getNamedQuery(QueryIpResponseEntity.FIND_BY_QUERY_IP);
        namedQuery.setParameter(QueryIpResponseEntity.IP_QUERY, ip);
        List<QueryIpResponseEntity> queryIpResponseEntityList =  namedQuery.getResultList();
        if (queryIpResponseEntityList.size() > 0) {
            return Optional.ofNullable(queryIpResponseEntityList.get(0));
        } else {
            return Optional.ofNullable(null);
        }
    }

    /**
     * Save Query ip response entity in database
     * @param queryIpResponseEntity query Ip ResponseEntity
     * @return created instance
     */
    public QueryIpResponseEntity create(QueryIpResponseEntity queryIpResponseEntity) {
       return persist(queryIpResponseEntity);
    }
}
