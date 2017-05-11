package net.snet.crm.infrastructure.addresses;

import net.snet.crm.domain.shared.data.Data;

public interface PlaceRepository
{
  long add(Data place);
  Data findById(long placeId);
}
