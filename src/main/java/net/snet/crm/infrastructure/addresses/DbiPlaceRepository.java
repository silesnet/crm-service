package net.snet.crm.infrastructure.addresses;

import net.snet.crm.domain.shared.data.Data;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.HandleCallback;

import static net.snet.crm.service.utils.Databases.*;

public class DbiPlaceRepository implements PlaceRepository
{
  private final DBI dbi;

  public DbiPlaceRepository(DBI dbi)
  {
    this.dbi = dbi;
  }

  @Override
  public long add(final Data record)
  {
    return dbi.withHandle(new HandleCallback<Long>()
    {
      @Override
      public Long withHandle(Handle handle) throws Exception
      {
        final Data place = findRecord(recordIdOf("places", "gps_cord", record.stringOf("gps_cord")), handle);
        return place.isEmpty() ? insertRecord("places", record.asMap(), handle) : place.longOf("place_id");
      }
    });
  }

  @Override
  public Data findById(long placeId)
  {
    return findRecord(recordIdOf("places", "place_id", placeId), dbi);
  }
}
