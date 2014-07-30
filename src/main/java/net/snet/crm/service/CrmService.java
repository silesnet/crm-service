package net.snet.crm.service;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import net.snet.crm.service.dao.CrmRepositoryJdbi;
import net.snet.crm.service.resources.*;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.skife.jdbi.v2.DBI;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.EnumSet;

public class CrmService extends Application<CrmConfiguration> {

	public static void main(String[] args) throws Exception {
		new CrmService().run(args);
	}

	@Override
	public String getName() {
		return "crm-service";
	}

	@Override
	public void initialize(Bootstrap<CrmConfiguration> bootstrap) {
		bootstrap.getObjectMapper().registerModule(new JodaModule());
		bootstrap.getObjectMapper().setDateFormat(new ISO8601DateFormat());
		bootstrap.addBundle(new MigrationsBundle<CrmConfiguration>() {
			@Override
			public DataSourceFactory getDataSourceFactory(CrmConfiguration configuration) {
				return configuration.getDataSourceFactory();
			}
		});
	}

	@Override
	public void run(CrmConfiguration configuration, Environment environment) throws ClassNotFoundException {
		final DBIFactory dbiFactory = new DBIFactory();
		final DBI dbi = dbiFactory.build(environment, configuration.getDataSourceFactory(), "postgresql");
		if (configuration.getJsonPrettyPrint()) {
			environment.getObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
		}

        FilterRegistration.Dynamic filters = environment.servlets().addFilter("CORS", new CrossOriginFilter());
        filters.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");
        filters.setInitParameter("allowedMethods", "GET,PUT,POST,DELETE");

		CrmRepositoryJdbi crmRepository = new CrmRepositoryJdbi(dbi);
		environment.jersey().register(new CustomerResource(dbi, crmRepository));
		environment.jersey().register(new AgreementResource(crmRepository));
		environment.jersey().register(new ServiceResource(crmRepository));
		environment.jersey().register(new ConnectionResource(crmRepository));
		environment.jersey().register(new DraftResource(dbi));
		environment.jersey().register(new RouterResource(dbi));
		environment.jersey().register(new NetworkResource(dbi));
		environment.jersey().register(new UserResource(dbi));
		environment.jersey().register(new ProductResource(dbi));
		environment.jersey().register(new ContractResource(dbi));
		environment.jersey().register(new BaseResource());

	}

}
