package net.snet.crm.infrastructure.addresses;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.Client;
import net.snet.crm.domain.shared.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;

public class RemoteAddressRepository implements AddressRepository
{
  private static final Logger LOG = LoggerFactory.getLogger(RemoteAddressRepository.class);

  private final Client http;
  private final String uri;

  public RemoteAddressRepository(Client http, String uri)
  {
    this.http = http;
    this.uri = uri;
  }

  @Override
  public List<Data> findByQuery(String query)
  {
    final URI queryUri = UriBuilder.fromUri(uri).queryParam("q", query).build();
    LOG.debug(queryUri.toString());
    try
    {
      final String response = http.resource(queryUri).get(String.class);
      List addresses = new ObjectMapper().readValue(response, List.class);
      return addresses;
    } catch (Exception e)
    {
      return Lists.newArrayList();
    }
  }

  @Override
  public Data findById(long addressId)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Data findByFk(String addressFk)
  {
    throw new UnsupportedOperationException();
  }
}
