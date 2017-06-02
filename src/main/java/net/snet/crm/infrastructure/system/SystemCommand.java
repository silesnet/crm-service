package net.snet.crm.infrastructure.system;

public interface SystemCommand extends Runnable {
  String name();
  String output();
}
