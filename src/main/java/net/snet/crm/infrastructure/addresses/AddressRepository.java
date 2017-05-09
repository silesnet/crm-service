package net.snet.crm.infrastructure.addresses;

import net.snet.crm.domain.shared.data.Data;

import java.util.List;

public interface AddressRepository
{
  List<Data> findByQuery(String query);
  Data findById(long addressId);
  Data findByFk(String addressFk);
}
