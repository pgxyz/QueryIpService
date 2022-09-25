# QueryIPService
# Introduction
The QueryIpService application provides an API to query GeoLocation information based on IP Address. 

# Overview

# Running The Application

To test the example application run the following commands.

* To build the application, install [Apache Maven](https://maven.apache.org/) and run following command to build installation jar.

        mvn clean install 

* To setup the h2 database and db table `ipquerytable` run.

        java -jar target/QueryIpService-1.0-SNAPSHOT.jar db migrate config.yml

* To run the server run.

        java -jar target/QueryIpService-1.0-SNAPSHOT.jar server config.yml

* Once the server is up and running, to fetch IP information, perform:
  
  `curl -H "Content-Type: application/json" -X GET http://localhost:8080/geolocation/ip/71.76.72.156 | json_pp`
```
{
    "as" : "AS11426 Charter Communications Inc",
    "city" : "Charlotte",
    "country" : "United States",
    "countryCode" : "US",
    "isp" : "Spectrum",
    "lat" : 35.1362,
    "lon" : -80.7673,
    "org" : "Road Runner",
    "persisted" : "true",
    "query" : "71.76.72.156",
    "region" : "NC",
    "regionName" : "North Carolina",
    "status" : "success",
    "timezone" : "America/New_York",
    "zip" : "28270"
}
```

Open API JSON Specification
---
`curl -H "Content-Type: application/json" -X GET http://localhost:8080/openapi.json | json_pp`

```
{
  "openapi" : "3.0.1",
  "info" : {
    "title" : "Query Geolocation by an IP API",
    "description" : "API to query Geolocation by an IP",
    "contact" : {
      "email" : "pradeep.gummi@gmail.com"
    }
  },
  "paths" : {
    "/geolocation/ip/{queryIp}" : {
      "get" : {
        "description" : "Fetch Geo Location by an IP",
        "operationId" : "queryIp",
        "parameters" : [ {
          "name" : "queryIp",
          "in" : "path",
          "required" : true,
          "schema" : {
            "type" : "string"
          }
        } ],
        "responses" : {
          "default" : {
            "description" : "default response",
            "content" : {
              "application/json" : {
                "schema" : {
                  "$ref" : "#/components/schemas/QueryIpResponseEntity"
                }
              }
            }
          }
        }
      }
    }
  },
  "components" : {
    "schemas" : {
      "QueryIpResponseEntity" : {
        "type" : "object",
        "properties" : {
          "query" : {
            "type" : "string"
          },
          "status" : {
            "type" : "string"
          },
          "country" : {
            "type" : "string"
          },
          "countryCode" : {
            "type" : "string"
          },
          "region" : {
            "type" : "string"
          },
          "regionName" : {
            "type" : "string"
          },
          "city" : {
            "type" : "string"
          },
          "zip" : {
            "type" : "string"
          },
          "lat" : {
            "type" : "number",
            "format" : "double"
          },
          "lon" : {
            "type" : "number",
            "format" : "double"
          },
          "timezone" : {
            "type" : "string"
          },
          "isp" : {
            "type" : "string"
          },
          "org" : {
            "type" : "string"
          },
          "as" : {
            "type" : "string"
          },
          "persisted" : {
            "type" : "string"
          }
        }
      }
    }
  }
}
```

Health Check
---

To see your applications health enter url 
`curl -H "Content-Type: application/json" -X GET http://localhost:8081/healthcheck  | json_pp`

```
{
   "deadlocks" : {
      "duration" : 0,
      "healthy" : true,
      "timestamp" : "2022-09-24T23:50:37.043-04:00"
   },
   "hibernate" : {
      "duration" : 3,
      "healthy" : true,
      "timestamp" : "2022-09-24T23:50:37.039-04:00"
   }
}
```

Design and Implementation Notes
---
- Application is implemented using DropWizard Java Framework and is comprised of the following classes:

`QueryIpResponseEntity` is entity object that comprises the GeoLocation information and is captured in `ipquerytable` table.

`QueryIpResponseDAO` is Data Access Object and provides access to `QueryIpResponseEntity`.

`QueryIPResource` is JAX RS Based Rest Resource that provides API response to the Client. It utilizes an in-memory cache to store the `QueryIpResponseEntity` for a preconfigured time. It performs the following actions: 
1. Validates the input Ip is in IPV4, IPV6, or domain name format
2. Checks if the result is in cache. If present, returns the result
3. If not in cache, queries the `ipquerytable` using the `QueryIpResponseDao`. If present, returns the result
4. If not in database table, then performs the remote API request to `http://ip-api.com/json/` and then saves the entry in the table and the cache to process future requests.

`migrations.xml` provides `ipquerytable` configuration and needs to be executed as per step 1 prior to running your application for the first time.
As the modules includes OpenAPI, REST Resource are initialized in the `QueryIPServiceApplication`.

`QueryIPServiceConfiguration` provides the configuration values used by the application and are configured in `config.yml`: 
1. `ipServiceUrl` : Url for the GeoLocation service client
2. `expireCacheInSeconds`: Expiry time in seconds for the cache used by `QueryIPResource`
3. `maxCacheSize`: Maximum number of items that can be stored in-memory cache
4. The configuration file also comprises of logging, database, and server configurations

Unit and Integration Testing
---
Application comprises of Unit tests using Mockito and Integration Tests for the following:
1. REST Resource: `QueryIPResourceTest`, `QueryIPResourceIntegrationTest`
2. DAO: `QueryIpResponseDAOTest`
3. Entity: `QueryIpResponseEntityTest`
