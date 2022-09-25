# QueryIPService
# Introduction
The QueryIPService application provides an API to query GeoLocation information based on IP Address. 

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
