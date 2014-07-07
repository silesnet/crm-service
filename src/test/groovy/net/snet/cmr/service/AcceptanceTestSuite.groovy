package net.snet.cmr.service

import com.yammer.dropwizard.config.Environment
import com.yammer.dropwizard.db.ManagedDataSourceFactory
import com.yammer.dropwizard.lifecycle.ServerLifecycleListener
import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.FileSystemResourceAccessor
import net.snet.crm.service.CrmConfiguration
import net.snet.crm.service.CrmService
import org.eclipse.jetty.server.Server
import org.junit.ClassRule
import org.junit.rules.ExternalResource
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Created by admin on 5.7.14.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses([
		CustomersATSpec.class,
		ServicesATSpec.class
])
class AcceptanceTestSuite {
	public static final DW = [:]

	@ClassRule
	public static ExternalResource service = new ExternalResource() {
		@Override
		protected void before() throws Throwable {
			println 'CRM service start...'
			def service = new CrmService() {
				@Override
				void run(CrmConfiguration configuration, Environment environment) throws ClassNotFoundException {
					environment.addServerLifecycleListener(new ServerLifecycleListener() {
						@Override
						void serverStarted(Server server) {
							AcceptanceTestSuite.DW.jettyServer = server
						}
					})
					// implement Liquibase update based on configuration and migrations.xml
					def dataSource = new ManagedDataSourceFactory().build(configuration.getDatabaseConfiguration())
					def liquibase = new Liquibase('src/main/resources/migrations.xml', new FileSystemResourceAccessor(), new JdbcConnection(dataSource.getConnection()))
					liquibase.update(null)
					super.run(configuration, environment)
				}
			}
			service.run(['server', 'src/dist/config/crm-service-dev.yml'] as String[])
		}

		@Override
		protected void after() {
			println 'CRM service stop'
			DW.jettyServer.stop()
		}
	}
}
