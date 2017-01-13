package net.snet.crm.infrastructure.network.access;

import org.skife.jdbi.v2.Handle;

public interface Action
{
  void perform(Handle handle);
}
