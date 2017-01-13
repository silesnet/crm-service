package net.snet.crm.infrastructure.network.access.action;

import net.snet.crm.infrastructure.network.access.Action;
import org.skife.jdbi.v2.Handle;

public final class NoAction implements Action
{
  public static NoAction INSTANCE = new NoAction();

  private NoAction() {
  }

  @Override
  public void perform(Handle handle) {
    // NOP
  }
}
