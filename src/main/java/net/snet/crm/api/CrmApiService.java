package net.snet.crm.api;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.jdbi.DBIFactory;
import net.snet.crm.api.dao.CustomerDao;
import net.snet.crm.api.dao.RegionDao;
import net.snet.crm.api.dao.ProductDao;
import net.snet.crm.api.dao.impl.CustomerDaoJdbi;
import net.snet.crm.api.resources.CustomerResource;
import net.snet.crm.api.resources.RegionResource;
import net.snet.crm.api.resources.ProductResource;
import org.skife.jdbi.v2.DBI;

public class CrmApiService extends Service<CrmApiConfiguration> {

	public static void main(String[] args) throws Exception {
		new CrmApiService().run(args);
	}

	@Override
	public void initialize(Bootstrap<CrmApiConfiguration> bootstrap) {
		bootstrap.setName("crm-api-service");
	}

	@Override
	public void run(CrmApiConfiguration configuration, Environment environment) throws ClassNotFoundException {
		final DBIFactory dbiFactory = new DBIFactory();
		final DBI dbi = dbiFactory.build(environment, configuration.getDatabaseConfiguration(), "postgresql");
		final RegionDao regionDao = dbi.onDemand(RegionDao.class);
		final ProductDao productDao = dbi.onDemand(ProductDao.class);
		final CustomerDao customerDao = new CustomerDaoJdbi(dbi);

		environment.addResource(new CustomerResource(customerDao));
		environment.addResource(new RegionResource(regionDao));
		environment.addResource(new ProductResource(productDao));

	}

}
