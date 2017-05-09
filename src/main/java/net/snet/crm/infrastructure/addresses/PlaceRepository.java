package net.snet.crm.infrastructure.addresses;

import net.snet.crm.domain.shared.data.Data;

public interface PlaceRepository
{
  Data findById(long placeId);
}
