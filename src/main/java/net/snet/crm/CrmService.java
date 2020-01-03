package net.snet.crm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.datatype.joda.JodaModule;
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
import net.snet.crm.infrastructure.addresses.DbiAddressRepository;
import net.snet.crm.infrastructure.addresses.DbiPlaceRepository;
import net.snet.crm.infrastructure.addresses.PlaceRepository;
import net.snet.crm.infrastructure.command.DefaultTaskFactory;
import net.snet.crm.infrastructure.command.TaskFactory;
import net.snet.crm.infrastructure.messaging.SmtpMessagingService;
import net.snet.crm.infrastructure.network.DefaultNetworkService;
import net.snet.crm.infrastructure.persistence.jdbi.*;
import net.snet.crm.infrastructure.system.FileSystemCommandFactory;
import net.snet.crm.infrastructure.system.SystemCommandFactory;
import net.snet.crm.service.CommandBroker;
import net.snet.crm.service.DefaultUserService;
import net.snet.crm.service.resources.*;
import net.snet.crm.service.resources.auth.AuthResource;
import net.snet.crm.service.resources.modules.DataModule;
import net.snet.crm.service.resources.modules.EventModule;
import net.snet.crm.service.utils.JsonUtil;
import net.snet.crm.service.utils.RuntimeExceptionMapper;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.ws.rs.client.Client;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
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

    FilterRegistration.Dynamic filter = environment.servlets().addFilter("CORS", CrossOriginFilter.class);
    filter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
    filter.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin,Authorization");
    filter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "OPTIONS,GET,PUT,POST,DELETE,HEAD,PATCH");
    filter.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, "true");
    filter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");

    final Client httpClient =
        new JerseyClientBuilder(environment)
            .using(configuration.getHttpClientConfiguration())
            .using(connectionRegistry())
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

    final NetworkService networkService = new DefaultNetworkService(
        systemCommandFactory,
        networkRepository);
    final SmtpMessagingService messagingService = new SmtpMessagingService(configuration.getSmsMessaging());
    final DbiAddressRepository addressRepository = new DbiAddressRepository(dbi, httpClient, configuration.getAddressServiceUri());
    final PlaceRepository placeRepository = new DbiPlaceRepository(dbi);

    final JerseyEnvironment jersey = environment.jersey();
    jersey.register(new AuthResource(dbi));
    jersey.register(new CustomerResource(dbi, crmRepository));
    jersey.register(new ServiceResource(crmRepository, networkRepository, todoRepository, addressRepository, placeRepository));
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
        addressRepository,
        placeRepository,
        dbi));
    jersey.register(new MessagingResource(messagingService));
    jersey.register(new EventResource(eventLog));
    jersey.register(new AdminResource(configuration.getVersion()));
    jersey.register(new AddressResource(addressRepository));
    jersey.register(new PlaceResource(placeRepository));
    jersey.register(new RuntimeExceptionMapper());

    final TaskFactory taskFactory = new DefaultTaskFactory(dbi, networkService, eventLog);
    final CommandBroker commandBroker = new CommandBroker(commandQueue, taskFactory);
    environment.lifecycle().manage(commandBroker);
  }

  private Registry<ConnectionSocketFactory> connectionRegistry() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
    final SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
        new SSLContextBuilder().loadTrustMaterial(null, trustStrategy()).build(),
        NoopHostnameVerifier.INSTANCE
    );
    return RegistryBuilder.<ConnectionSocketFactory>create()
        .register("http", PlainConnectionSocketFactory.getSocketFactory())
        .register("https", sslConnectionSocketFactory)
        .build();
  }

  private TrustStrategy trustStrategy() {
    return (chain, authType) -> true;
  }
}
