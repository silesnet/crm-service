package net.snet.crm.infrastructure.network;

import net.snet.crm.domain.model.network.NetworkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnmpNetworkService implements NetworkService {

  private static final Logger logger = LoggerFactory.getLogger(SnmpNetworkService.class);

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
}