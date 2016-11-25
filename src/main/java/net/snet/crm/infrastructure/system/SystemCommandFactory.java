package net.snet.crm.infrastructure.system;

public interface SystemCommandFactory {

  SystemCommand systemCommand(String command, String... args);

}
