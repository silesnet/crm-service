package net.snet.crm.service;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.sun.jersey.api.client.Client;
import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import net.snet.crm.service.dao.CrmRepositoryJdbi;
import net.snet.crm.service.resources.*;

import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.skife.jdbi.v2.DBI;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
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
	public void run(CrmConfiguration configuration, Environment environment) throws ClassNotFoundException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, URISyntaxException {
		final DBIFactory dbiFactory = new DBIFactory();
		final DBI dbi = dbiFactory.build(environment, configuration.getDataSourceFactory(), "postgresql");
		if (configuration.getJsonPrettyPrint()) {
			environment.getObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
		}

        FilterRegistration.Dynamic filters = environment.servlets().addFilter("CORS", new CrossOriginFilter());
        filters.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");
        filters.setInitParameter("allowedMethods", "GET,PUT,POST,DELETE");

		final Client httpClient =
				new JerseyClientBuilder(environment)
						.using(configuration.getHttpClientConfiguration())
						.using(schemeRegistry())
						.using(environment)
						.build("crm-service-http-client");

		CrmRepositoryJdbi crmRepository = new CrmRepositoryJdbi(dbi);
		environment.jersey().register(new CustomerResource(dbi, crmRepository));
		environment.jersey().register(new AgreementResource(crmRepository));
		environment.jersey().register(new ServiceResource(crmRepository));
		environment.jersey().register(new ConnectionResource(crmRepository));
		environment.jersey().register(new DraftResource(dbi));
		environment.jersey().register(new RouterResource(dbi));
		environment.jersey().register(new NetworkResource(dbi));
		environment.jersey().register(new UserResource(dbi, crmRepository,
				new DefaultUserService(httpClient, configuration.getUserServiceUri())));
		environment.jersey().register(new ProductResource(dbi));
		environment.jersey().register(new ContractResource(dbi));
		environment.jersey().register(new BaseResource());
	}

	private SchemeRegistry schemeRegistry() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		SSLSocketFactory socketFactory = new SSLSocketFactory(trustStrategy(), SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
		schemeRegistry.register(new Scheme("https", 443, socketFactory));
		schemeRegistry.register(new Scheme("https", 8443, socketFactory));
		return schemeRegistry;
	}

	private TrustStrategy trustStrategy() {
		return new TrustStrategy() {
			@Override
			public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				return true;
			}
		};
	}


}
