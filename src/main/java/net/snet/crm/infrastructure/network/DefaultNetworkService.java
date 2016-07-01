package net.snet.crm.infrastructure.network;

import net.snet.crm.domain.model.network.NetworkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;

public class DefaultNetworkService implements NetworkService {

  private static final Logger logger = LoggerFactory.getLogger(DefaultNetworkService.class);

  private final String kickPppoeUserCommand;

  public DefaultNetworkService(String kickPppoeUserCommand) {
    this.kickPppoeUserCommand = kickPppoeUserCommand;
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
    final Runnable task = new Runnable() {
      @Override
      public void run() {
        try {
          Process process = new ProcessBuilder(kickPppoeUserCommand, master, login)
              .redirectErrorStream(true)
              .redirectOutput(ProcessBuilder.Redirect.INHERIT)
              .start();
          int error = process.waitFor();
          if (error != 0) {
            throw new RuntimeException("error code: " + error);
          }
          logger.info("PPPoE '{}' kicked '{}'", master, login);
        } catch (Exception e) {
          logger.error("failed executing '{}'", kickPppoeUserCommand, e);
        }
      }
    };
    final Thread thread = new Thread(task);
    thread.start();
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
