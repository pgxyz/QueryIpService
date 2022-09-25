package com.hilton.queryservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.core.Configuration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Query Service Configuration class to read properties from config.yml
 */
public class QueryIPServiceConfiguration extends Configuration {

    @NotEmpty
    private String ipServiceUrl;

    @NotNull
    private int expireCacheInSeconds;

    @NotNull
    private int maxCacheSize;

    @Valid
    @NotNull
    private DataSourceFactory database = new DataSourceFactory();

    public String getIpServiceUrl() {
        return ipServiceUrl;
    }

    public void setIpServiceUrl(String ipServiceUrl) {
        this.ipServiceUrl = ipServiceUrl;
    }

    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.database = dataSourceFactory;
    }

    public int getExpireCacheInSeconds() {
        return expireCacheInSeconds;
    }

    public void setExpireCacheInSeconds(int expireCacheInSeconds) {
        this.expireCacheInSeconds = expireCacheInSeconds;
    }

    public int getMaxCacheSize() {
        return maxCacheSize;
    }

    public void setMaxCacheSize(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }
}
