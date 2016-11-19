package net.snet.crm.domain.shared.command;

import net.snet.crm.domain.shared.GenericLongId;

public class CommandId extends GenericLongId<CommandId> {
  public static final CommandId NONE = new CommandId();

  public CommandId(long id) {
    super(id);
  }
  private CommandId() { super();}
}
