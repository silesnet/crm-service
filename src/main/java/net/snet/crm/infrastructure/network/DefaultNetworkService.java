package net.snet.crm.infrastructure.network;

import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.model.network.NetworkService;
import net.snet.crm.domain.shared.data.Data;
import net.snet.crm.domain.shared.data.MapData;
import net.snet.crm.infrastructure.system.SystemCommandFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

import static net.snet.crm.infrastructure.system.SystemCommandRunner.executeSystemCommand;

public class DefaultNetworkService implements NetworkService {

  private static final Logger logger = LoggerFactory.getLogger(DefaultNetworkService.class);

  private final SystemCommandFactory commandFactory;
  private final NetworkRepository networkRepository;

  public DefaultNetworkService(SystemCommandFactory commandFactory, NetworkRepository networkRepository) {
    this.commandFactory = commandFactory;
    this.networkRepository = networkRepository;
  }

  @Override
  public void enableService(long serviceId) {
    logger.debug("enabling service with id '{}'...", serviceId);
    final Data pppoe = MapData.of(networkRepository.findServicePppoe(serviceId));
    final boolean hasPppoe = !pppoe.asMap().isEmpty();
    if (hasPppoe) {
      kickPppoeUser(pppoe.stringOf("master"), pppoe.stringOf("login"));
    }
    final Data dhcp = MapData.of(networkRepository.findServiceDhcp(serviceId));
    final boolean hasDhcp = !dhcp.asMap().isEmpty();
    if (hasDhcp) {
      enableSwitchPort(dhcp.stringOf("switch"), dhcp.intOf("port"));
    }
    if (!hasPppoe && !hasDhcp) {
      executeSystemCommand(commandFactory.systemCommand(
          "sendEmail",
          "-a", "podpora@silesnet.cz",
          "-s", "enable debtor's service: " + serviceId,
          "-m", "/SIS"));
    }
    logger.info("enabled service with id '{}'", serviceId);
  }

  @Override
  public void disableService(long serviceId) {
    logger.debug("disabling service with id '{}'...", serviceId);
    final Data pppoe = MapData.of(networkRepository.findServicePppoe(serviceId));
    final boolean hasPppoe = !pppoe.asMap().isEmpty();
    if (hasPppoe) {
      kickPppoeUser(pppoe.stringOf("master"), pppoe.stringOf("login"));
    }
    final Data dhcp = MapData.of(networkRepository.findServiceDhcp(serviceId));
    final boolean hasDhcp = !dhcp.asMap().isEmpty();
    if (hasDhcp) {
      disableSwitchPort(dhcp.stringOf("switch"), dhcp.intOf("port"));
    }
    if (!hasPppoe && !hasDhcp) {
      executeSystemCommand(commandFactory.systemCommand(
          "sendEmail",
          "-a", "podpora@silesnet.cz",
          "-s", "disable debtor's service: " + serviceId,
          "-m", "/SIS"));
    }
    logger.info("disabled service with id '{}'", serviceId);
  }

  @Override
  public void enableSwitchPort(final String switchName, final int port) {
    executeSystemCommand(commandFactory.systemCommand(
        "configureDhcpPort",
        "-s", switchName,
        "-p", "" + port,
        "-v", "1"));
    logger.info("enabled network switch port '{}/{}'", switchName, port);
  }

  @Override
  public void disableSwitchPort(final String switchName, final int port) {
    executeSystemCommand(commandFactory.systemCommand(
        "configureDhcpPort",
        "-s", switchName,
        "-p", "" + port,
        "-v", "2"));
    logger.info("disabled network switch port '{}/{}'", switchName, port);
  }

  @Override
  public boolean isSwitchPortEnabled(final String switchName, final int port) {
    return false;
  }

  @Override
  public void kickPppoeUser(final String master, final String login) {
    executeSystemCommand(commandFactory.systemCommand(
        "kickPppoeUser",
        "-d", master,
        "-u", login));
    logger.info("kicked '{}' from '{}' network device", login, master);
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
