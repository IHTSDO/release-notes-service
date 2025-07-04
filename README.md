# Release Notes Service
## Overview
SNOMED CT Release Notes Service is a standalone service for management of release notes. 
It is also integrated into Authoring Platform and SNOMED CT Browser (read-only access). 

## Capabilities
- Perform CRUD operations on release notes line items
- Promote, version and publish line items
- Read published release notes

## Permissions
Set property _ims-security.roles.enabled_ to _true_ to manage user permissions. You may also need update _snowstorm.url_ to point to the terminology server URL. 

## Quick Start
Use Maven to build the executable jar and run:
```bash
mvn clean package
java -Xmx1g -jar target/release-notes-service*.jar
```
Please note that by default tests are executed in Elasticsearch Docker container so make sure you have Docker installed and running.

If you want to use your local Elasticsearch installation, set _useLocalElasticsearch_ to _true_ in [TestConfig.java](src/test/java/org/snomed/release/note/TestConfig.java).

Access the service **API documentation** at [http://localhost:8087/release-notes-service/swagger-ui/index.html](http://localhost:8081/release-notes-service/swagger-ui/index.html).

## Setup
To use this service you need to install Elasticsearch (minimum version 8.7.1 ).

Elasticsearch URLs, index prefix, username and password can be found in [application.properties](src/main/resources/application.properties).

Elasticsearch settings (number of shards and replicas) can be found in [elasticsearch-settings.json](src/main/resources/elasticsearch-settings.json). These settings are only applied once when indices are created.

### Configuration options
The default configuration of this Spring Boot application can be found in [application.properties](src/main/resources/application.properties). The defaults can be overridden using command line arguments, for example set a different HTTP port:
```bash
java -Xmx1g -jar target/release-notes-service*.jar --server.port=8099
```
For other options see [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html).

The default username and password (test-user:test-password) can be changed using the _security.user.name_ and _security.user.password_ properties.