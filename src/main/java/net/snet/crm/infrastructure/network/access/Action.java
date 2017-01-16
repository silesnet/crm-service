package net.snet.crm.infrastructure.network.access;

import org.skife.jdbi.v2.Handle;

import java.util.List;

public interface Action
{
  List<String> perform(long serviceId, Handle handle);
}
