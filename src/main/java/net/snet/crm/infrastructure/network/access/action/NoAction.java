package net.snet.crm.infrastructure.network.access.action;

import net.snet.crm.domain.shared.data.Data;
import net.snet.crm.infrastructure.network.access.Action;
import org.skife.jdbi.v2.Handle;

import java.util.ArrayList;
import java.util.List;

public final class NoAction implements Action
{
  public static NoAction INSTANCE = new NoAction();

  private NoAction() {
  }

  @Override
  public List<String> perform(long serviceId, Data draft, Handle handle) {
    return new ArrayList<>();
  }
}
