package com.hilton.queryservice.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static io.dropwizard.jackson.Jackson.newObjectMapper;
import static org.assertj.core.api.Assertions.assertThat;

public class QueryIpResponseEntityTest {

    private static final ObjectMapper MAPPER = newObjectMapper();

    @Test
    void serializesToJSON() throws Exception {
        final QueryIpResponseEntity queryIpResponseEntity = new QueryIpResponseEntity();
        queryIpResponseEntity.setQuery("111.11.11.111");
        queryIpResponseEntity.setStatus("success");
        queryIpResponseEntity.setCountry("us");
        queryIpResponseEntity.setCountryCode("us");
        queryIpResponseEntity.setRegion("nc");
        queryIpResponseEntity.setRegionName("nc");
        queryIpResponseEntity.setCity("charlotte");
        queryIpResponseEntity.setZip("28170");
        queryIpResponseEntity.setLat(20.90);
        queryIpResponseEntity.setLon(20.90);
        queryIpResponseEntity.setTimezone("est");
        queryIpResponseEntity.setIsp("spectrum");
        queryIpResponseEntity.setOrg("spectrum");
        queryIpResponseEntity.setAs("as");
        queryIpResponseEntity.setPersisted("true");


        final String expected = MAPPER.writeValueAsString(
                MAPPER.readValue(getClass().getResource("/queryip.json"), QueryIpResponseEntity.class));

        assertThat(MAPPER.writeValueAsString(queryIpResponseEntity)).isEqualTo(expected);
    }

    @Test
    public void deserializesFromJSON() throws Exception {
        final QueryIpResponseEntity queryIpResponseEntity = new QueryIpResponseEntity();
        queryIpResponseEntity.setQuery("111.11.11.111");
        queryIpResponseEntity.setStatus("success");
        queryIpResponseEntity.setCountry("us");
        queryIpResponseEntity.setCountryCode("us");
        queryIpResponseEntity.setRegion("nc");
        queryIpResponseEntity.setRegionName("nc");
        queryIpResponseEntity.setCity("charlotte");
        queryIpResponseEntity.setZip("28170");
        queryIpResponseEntity.setLat(20.90);
        queryIpResponseEntity.setLon(20.90);
        queryIpResponseEntity.setTimezone("est");
        queryIpResponseEntity.setIsp("spectrum");
        queryIpResponseEntity.setOrg("spectrum");
        queryIpResponseEntity.setAs("as");
        queryIpResponseEntity.setPersisted("true");
        assertThat(MAPPER.readValue(getClass().getResource("/queryip.json"), QueryIpResponseEntity.class))
                .isEqualTo(queryIpResponseEntity);
    }
}
