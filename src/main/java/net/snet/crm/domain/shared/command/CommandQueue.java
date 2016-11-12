package net.snet.crm.domain.shared.command;

import net.snet.crm.domain.shared.data.Data;

import java.util.List;

public interface CommandQueue {
  Command submit(Commands command, String entity, String entityId, Data data);
  List<Command> nextOf(Commands command, int batch);
  Command process(CommandId id);
  Command complete(CommandId id);
  Command fail(CommandId id);
}
