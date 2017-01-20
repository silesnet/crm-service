package net.snet.crm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.sun.jersey.api.client.Client;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import net.snet.crm.domain.model.agreement.AgreementRepository;
import net.snet.crm.domain.model.network.NetworkService;
import net.snet.crm.domain.shared.command.CommandQueue;
import net.snet.crm.domain.shared.event.EventLog;
import net.snet.crm.infrastructure.command.DefaultTaskFactory;
import net.snet.crm.infrastructure.command.TaskFactory;
import net.snet.crm.infrastructure.messaging.SmtpMessagingService;
import net.snet.crm.infrastructure.network.DefaultNetworkService;
import net.snet.crm.infrastructure.persistence.jdbi.DbiAgreementRepository;
import net.snet.crm.infrastructure.persistence.jdbi.DbiCommandQueue;
import net.snet.crm.infrastructure.persistence.jdbi.DbiEventLog;
import net.snet.crm.infrastructure.persistence.jdbi.DbiNetworkRepository;
import net.snet.crm.infrastructure.system.FileSystemCommandFactory;
import net.snet.crm.infrastructure.system.SystemCommandFactory;
import net.snet.crm.service.CommandBroker;
import net.snet.crm.service.DefaultUserService;
import net.snet.crm.infrastructure.persistence.jdbi.DbiCrmRepository;
import net.snet.crm.infrastructure.persistence.jdbi.DbiDraftRepository;
import net.snet.crm.infrastructure.persistence.jdbi.DbiTodoRepository;
import net.snet.crm.service.resources.*;
import net.snet.crm.service.resources.modules.DataModule;
import net.snet.crm.service.resources.modules.EventModule;
import net.snet.crm.service.utils.JsonUtil;
import net.snet.crm.service.utils.RuntimeExceptionMapper;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private static final Logger LOG = LoggerFactory.getLogger(CrmService.class);

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
    bootstrap.getObjectMapper().registerModule(new DataModule());
    bootstrap.getObjectMapper().registerModule(new EventModule());
    bootstrap.getObjectMapper().setDateFormat(new ISO8601DateFormat());
    bootstrap.addBundle(new MigrationsBundle<CrmConfiguration>() {
      @Override
      public DataSourceFactory getDataSourceFactory(CrmConfiguration configuration) {
        return configuration.getDataSourceFactory();
      }
    });
    bootstrap.addBundle(new AssetsBundle("/favicon.ico"));
  }

  @Override
  public void run(CrmConfiguration configuration, Environment environment) throws ClassNotFoundException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, URISyntaxException {
    LOG.info("Starting CRM service...");
    final DBIFactory dbiFactory = new DBIFactory();
    final DBI dbi = dbiFactory.build(environment, configuration.getDataSourceFactory(), "postgresql");
    final ObjectMapper mapper = environment.getObjectMapper();
    if (configuration.getJsonPrettyPrint()) {
      mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    JsonUtil.configure(mapper);

    FilterRegistration.Dynamic filters = environment.servlets().addFilter("CORS", new CrossOriginFilter());
    filters.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");
    filters.setInitParameter("allowedMethods", "GET,PUT,POST,DELETE");

    final Client httpClient =
        new JerseyClientBuilder(environment)
            .using(configuration.getHttpClientConfiguration())
            .using(schemeRegistry())
            .using(environment)
            .build("crm-service-http-client");

    DbiCrmRepository crmRepository = new DbiCrmRepository(dbi);
    final DbiDraftRepository draftRepository = new DbiDraftRepository(dbi, mapper);
    final AgreementRepository agreementRepository = new DbiAgreementRepository(dbi, mapper);
    final DbiNetworkRepository networkRepository = new DbiNetworkRepository(dbi);
    final DbiTodoRepository todoRepository = new DbiTodoRepository(dbi);
    final CommandQueue commandQueue = new DbiCommandQueue(dbi, mapper);
    final EventLog eventLog = new DbiEventLog(dbi);

    final SystemCommandFactory systemCommandFactory = new FileSystemCommandFactory(configuration.getSystemCommandHome());

    final NetworkService networkService = new DefaultNetworkService(systemCommandFactory, networkRepository);
    final SmtpMessagingService messagingService = new SmtpMessagingService(configuration.getSmsMessaging());

    final JerseyEnvironment jersey = environment.jersey();
    jersey.register(new CustomerResource(dbi, crmRepository));
    jersey.register(new ServiceResource(crmRepository, networkRepository, todoRepository));
    jersey.register(new NetworkResource(dbi, networkService));
    jersey.register(new UserResource(dbi, crmRepository,
        new DefaultUserService(httpClient, configuration.getUserServiceUri(), crmRepository)));
    jersey.register(new ProductResource(dbi));
    jersey.register(new DraftResource2(
        draftRepository,
        crmRepository,
        agreementRepository,
        networkRepository,
        networkService,
        dbi));
    jersey.register(new MessagingResource(messagingService));
    jersey.register(new EventResource(eventLog));
    jersey.register(new RuntimeExceptionMapper());

    final TaskFactory taskFactory = new DefaultTaskFactory(dbi, networkService, eventLog);
    final CommandBroker commandBroker = new CommandBroker(commandQueue, taskFactory);
    environment.lifecycle().manage(commandBroker);
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
