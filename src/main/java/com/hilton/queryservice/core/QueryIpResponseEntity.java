package com.hilton.queryservice.core;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

/**
 * Query ip response entity table mapped to ipquerytable in database
 */
@Entity
@Table(name = "ipquerytable")
@NamedQuery(name = "com.hilton.queryservice.core.QueryIpResponseEntity.findByQuery", query = "SELECT p from QueryIpResponseEntity p where p.query = :ipQuery")
public class QueryIpResponseEntity implements Serializable {

    public static String FIND_BY_QUERY_IP = "com.hilton.queryservice.core.QueryIpResponseEntity.findByQuery";
    public static String IP_QUERY = "ipQuery";
    private String query;
    private String status;
    private String country;
    private String countryCode;
    private String region;
    private String regionName;
    private String city;
    private String zip;
    private double lat;
    private double lon;
    private String timezone;
    private String isp;
    private String org;
    private String as;
    private String persisted;

    public QueryIpResponseEntity() {
    }

    @Id()
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getIsp() {
        return isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    @Column(name = "asColumn")
    public String getAs() {
        return as;
    }

    public void setAs(String as) {
        this.as = as;
    }

    public String getPersisted() {
        return persisted;
    }

    public void setPersisted(String persisted) {
        this.persisted = persisted;
    }

    public static QueryIpResponseEntity copyValuesFrom(QueryIpResponseEntity queryIpResponseEntity) {
        QueryIpResponseEntity newObject = new QueryIpResponseEntity();
        newObject.setQuery(queryIpResponseEntity.getQuery());
        newObject.setStatus(queryIpResponseEntity.getStatus());
        newObject.setCountry(queryIpResponseEntity.getCountry());
        newObject.setCountryCode(queryIpResponseEntity.getCountryCode());
        newObject.setRegion(queryIpResponseEntity.getRegion());
        newObject.setRegionName(queryIpResponseEntity.getRegionName());
        newObject.setCity(queryIpResponseEntity.getCity());
        newObject.setZip(queryIpResponseEntity.getZip());
        newObject.setLat(queryIpResponseEntity.getLat());
        newObject.setLon(queryIpResponseEntity.getLon());
        newObject.setTimezone(queryIpResponseEntity.getTimezone());
        newObject.setIsp(queryIpResponseEntity.getIsp());
        newObject.setOrg(queryIpResponseEntity.getOrg());
        newObject.setAs(queryIpResponseEntity.getAs());
        newObject.setPersisted(queryIpResponseEntity.getPersisted());
        return newObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryIpResponseEntity that = (QueryIpResponseEntity) o;
        return Double.compare(that.lat, lat) == 0 && Double.compare(that.lon, lon) == 0 && query.equals(that.query) && Objects.equals(status, that.status) && Objects.equals(country, that.country) && Objects.equals(countryCode, that.countryCode) && Objects.equals(region, that.region) && Objects.equals(regionName, that.regionName) && Objects.equals(city, that.city) && Objects.equals(zip, that.zip) && Objects.equals(timezone, that.timezone) && Objects.equals(isp, that.isp) && Objects.equals(org, that.org) && Objects.equals(as, that.as) && Objects.equals(persisted, that.persisted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(query, status, country, countryCode, region, regionName, city, zip, lat, lon, timezone, isp, org, as, persisted);
    }
}
