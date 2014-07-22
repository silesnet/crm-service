package net.snet.crm.service.dao;

import org.joda.time.DateTime;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.mixins.CloseMe;
import org.skife.jdbi.v2.sqlobject.mixins.GetHandle;
import org.skife.jdbi.v2.sqlobject.mixins.Transactional;
import org.skife.jdbi.v2.tweak.HandleCallback;

import java.sql.Timestamp;
import java.util.Map;

public class CustomerRepositoryJdbi implements CustomerRepository {

	private final CustomerDatabase customerDb;

	public CustomerRepositoryJdbi(DBI dbi) {
		customerDb = dbi.onDemand(CustomerDatabase.class);
	}

	@Override
	public Map<String, Object> insert(Map<String, Object> customer) {
		customerDb.begin();
		long id = customerDb.nextId();
		customerDb.insert(id, customer.get("name").toString(), new Timestamp(new DateTime().getMillis()));
		customerDb.commit();
		return findById(id);
	}

	public Map<String, Object> findById(final long id) {
		return customerDb.withHandle(new HandleCallback<Map<String, Object>>() {
			@Override
			public Map<String, Object> withHandle(Handle handle) throws Exception {
				return handle.createQuery("SELECT * FROM customers WHERE id=:id")
						.bind("id", id)
						.first();
			}
		});
	}

	public interface CustomerDatabase extends Transactional<CustomerDatabase>, GetHandle, CloseMe {

		@SqlQuery("SELECT max(id) + 1 FROM customers")
		public abstract long nextId();

		@SqlUpdate("INSERT INTO customers (id, history_id, name, public_id, inserted_on, is_active) " +
				"VALUES (:id, 1, :name, '9999999', :inserted_on, false)")
		public abstract void insert(@Bind("id") long id, @Bind("name") String name,
		                            @Bind("inserted_on") Timestamp insertedOn);

	}
}
