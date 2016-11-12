# CRM Service

Implements business logic of CRM system.

## Resources
**Customer** - person receiving provided service.

**Agreement** - agreement between customer and company.

**Service** - product being provided.

**Connection** - network link from customer's device to operator's device.

**Account** - customers invoicing record.

**Address** - postal address.

## REST API

### POST /customers
Create new Customer.

### POST /customers/{id}/agreements
Create new Agreement with the Customer.

### POST /agreements/{id}/services
Create new Service under specific Agreement.

### POST /services/{id}/connections
Create new Connection for specific Service.

### PUT /connections/{id}
Update Connection parameters.

All value should be sent in JSON (application/vnd.api+json), http://jsonapi.org/

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

[PostgreSql](http://www.postgresql.org/) databse is used. Database connection details are defined in `src/dist/config/crm-api-dev.yml`. Initialization script (containing sample value) can be found in `src/test/resources/db` folder.

## Technology
* Dropwizard (http://dropwizard.codahale.com)
* Gradle (http://www.gradle.org/)
* JSON API (http://jsonapi.org/)
* RAML (http://raml.org)
