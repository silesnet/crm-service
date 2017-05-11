package net.snet.crm.infrastructure.addresses;

import com.sun.jersey.api.client.Client;
import net.snet.crm.domain.shared.data.Data;
import net.snet.crm.service.utils.Databases;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.HandleCallback;

import java.util.List;

import static net.snet.crm.service.utils.Databases.findRecord;

public class DbiAddressRepository implements AddressRepository
{
  private final DBI dbi;
  private final AddressRepository remoteAddressRepository;
  public DbiAddressRepository(DBI dbi, Client httpClient, String addressServiceUri)
  {
    this.dbi = dbi;
    remoteAddressRepository = new RemoteAddressRepository(httpClient, addressServiceUri);
  }

  @Override
  public List<Data> findByQuery(final String query)
  {
    return remoteAddressRepository.findByQuery(query);
//    return dbi.withHandle(new HandleCallback<List<Data>>()
//    {
//      @Override
//      public List<Data> withHandle(Handle handle) throws Exception
//      {
//        return Databases.findRecords(
//            query,
//            Data.EMPTY,
//            handle
//        );
//      }
//    });


  }

  @Override
  public Data findById(long addressId)
  {
    return findRecord(Databases.recordIdOf("addresses", "address_id", addressId), dbi);
  }

  @Override
  public Data findByFk(String addressFk)
  {
    return findRecord(Databases.recordIdOf("addresses", "address_fk", addressFk), dbi);
  }
}
