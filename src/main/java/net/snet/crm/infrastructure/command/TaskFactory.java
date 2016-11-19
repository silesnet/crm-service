package net.snet.crm.infrastructure.command;

import net.snet.crm.domain.shared.command.Command;

public interface TaskFactory {
  Task of(Command command);
}
