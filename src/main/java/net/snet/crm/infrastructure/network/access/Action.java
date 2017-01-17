package net.snet.crm.infrastructure.network.access;

import net.snet.crm.domain.shared.data.Data;
import org.skife.jdbi.v2.Handle;

import java.util.List;

public interface Action
{
  List<String> perform(long serviceId, Data draft, Handle handle);
}
