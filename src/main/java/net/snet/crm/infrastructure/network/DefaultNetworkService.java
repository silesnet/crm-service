package net.snet.crm.infrastructure.network;

import net.snet.crm.domain.model.network.NetworkService;
import net.snet.crm.infrastructure.system.SystemCommand;
import net.snet.crm.infrastructure.system.SystemCommandFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;

public class DefaultNetworkService implements NetworkService {

  private static final Logger logger = LoggerFactory.getLogger(DefaultNetworkService.class);

  private final SystemCommandFactory commandFactory;

  public DefaultNetworkService(SystemCommandFactory commandFactory) {
    this.commandFactory = commandFactory;
  }

  @Override
  public void enableService(long serviceId) {
    logger.debug("enabling service with id '{}'...", serviceId);
    logger.info("enabled service with id '{}'", serviceId);
  }

  @Override
  public void disableService(long serviceId) {
    logger.debug("disabling service with id '{}'...", serviceId);
    logger.info("disabled service with id '{}'", serviceId);
  }

  @Override
  public void enableSwitchPort(final String switchName, final int port) {
    final Runnable task = new Runnable() {
      @Override
      public void run() {
        logger.info("enabled switch port '{}/{}'", switchName, port);
      }
    };
    final Thread thread = new Thread(task);
    thread.start();
  }

  @Override
  public void disableSwitchPort(final String switchName, final int port) {
    final Runnable task = new Runnable() {
      @Override
      public void run() {
        logger.info("disabled switch port '{}/{}'", switchName, port);
      }
    };
    final Thread thread = new Thread(task);
    thread.start();
  }

  @Override
  public boolean isSwitchPortEnabled(final String switchName, final int port) {
    return false;
  }

  @Override
  public void kickPppoeUser(final String master, final String login) {
    final SystemCommand command = commandFactory.systemCommand("kickPppoeUser", master, login);
    final Runnable task = new Runnable() {
      @Override
      public void run() {
        try {
          command.run();
          logger.info("command '{}' completed", command.name());
        } catch (Exception e) {
          logger.error("failed executing '{}'", command.name(), e);
        }
      }
    };
    final Thread thread = new Thread(task);
    thread.start();
    try {
      thread.join();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean isIpReachable(String ip) {
    try {
      return InetAddress.getByName(ip).isReachable(1000);
    } catch (IOException e) {
      return false;
    }
  }

}
