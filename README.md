# CRM Service

Implements business logic of CRM system.

## Building

Project uses [gradle](http://www.gradle.org/) build tool.

### Runing crm-api service

```
> gradlew run
```

Point your browser at http://localhost:8080/customers

### Distribution build

```
> gradlew distZip
```

You would find distribution package in `build/distributions`. In order to run the service
you need to unzip the packege and `cd` into it.

```
> bin/crm-api server config/crm-api-dev.yml
```

### Database

[PostgreSql](http://www.postgresql.org/) databse is used. Database connection details are defined in `src/dist/config/crm-api-dev.yml`. Initialization script (containing sample data) can be found in `src/test/resources/db` folder.

## REST API

API specification is provided at http://silesnet.github.io/.

This project aims at implementing following resource APIs:
* customers
* services
* products
* regions

All data should be sent in JSON (application/vnd.api+json), http://jsonapi.org/

## Technology
* Dropwizard (http://dropwizard.codahale.com)
* Gradle (http://www.gradle.org/)
* JSON API (http://jsonapi.org/)
* RAML (http://raml.org)
