package net.snet.crm.infrastructure.addresses;

import net.snet.crm.domain.shared.data.Data;
import org.skife.jdbi.v2.DBI;

public class DbiPlaceRepository implements PlaceRepository
{
  private final DBI dbi;

  public DbiPlaceRepository(DBI dbi)
  {
    this.dbi = dbi;
  }

  @Override
  public Data findById(long placeId)
  {
    return null;
  }
}
