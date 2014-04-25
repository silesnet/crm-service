package net.snet.crm.service;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.jdbi.DBIFactory;
import net.snet.crm.service.filter.CorsHeadersFilter;
import net.snet.crm.service.resources.*;
import org.skife.jdbi.v2.DBI;

public class CrmService extends Service<CrmConfiguration> {

    public static void main(String[] args) throws Exception {
        new CrmService().run(args);
    }

    @Override
    public void initialize(Bootstrap<CrmConfiguration> bootstrap) {
        bootstrap.setName("crm-service");
    }

    @Override
    public void run(CrmConfiguration configuration, Environment environment) throws ClassNotFoundException {
        final DBIFactory dbiFactory = new DBIFactory();
        final DBI dbi = dbiFactory.build(environment, configuration.getDatabaseConfiguration(), "postgresql");
        if (configuration.getJsonPrettyPrint()) {
            environment.getObjectMapperFactory().enable(SerializationFeature.INDENT_OUTPUT);
        }

        environment.addFilter(new CorsHeadersFilter(), "/*");
        environment.addResource(new CustomerResource(dbi));
        environment.addResource(new DraftResource(dbi));
        environment.addResource(new RouterResource(dbi));
        environment.addResource(new ProductResource(dbi));
        environment.addResource(new ContractResource(dbi));
        environment.addResource(new BaseResource());

    }

}