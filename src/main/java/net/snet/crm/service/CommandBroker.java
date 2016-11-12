package net.snet.crm.service;

import io.dropwizard.lifecycle.Managed;
import net.snet.crm.domain.shared.command.Command;
import net.snet.crm.domain.shared.command.CommandQueue;
import net.snet.crm.domain.shared.command.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CommandBroker implements Managed {
  private static final Logger log = LoggerFactory.getLogger(CommandBroker.class);
  private final Thread thread;
  private final Broker broker;

  public CommandBroker(CommandQueue commandQueue) {
    broker = new Broker(commandQueue);
    thread = new Thread(this.broker, "command-broker");
  }

  @Override
  public void start() throws Exception {
    thread.start();
  }

  @Override
  public void stop() throws Exception {
    broker.terminate();
    thread.join();
  }

  private class Broker implements Runnable {
    private volatile boolean isRunning = true;
    private final CommandQueue commandQueue;

    public Broker(CommandQueue commandQueue) {
      this.commandQueue = commandQueue;
    }

    @Override
    public void run() {
      log.info("command broker starting...");
      Command current = null;
      while (isRunning) {
        try {
          final List<Command> commands = commandQueue.nextOf(Commands.DISCONNECT, 1);
          for (Command command : commands) {
            current = command;
            log.info("processing command '{}' for '{}/{}'...",
                command.name(), command.entity(), command.entityId());
            commandQueue.process(command.id());

            //TODO: do the work here

            //TODO: find customer's services, change status to debtor
            //PPPoE -> kick, DHCP -> port down, email otherwise
            //change customer status to debtor

            commandQueue.complete(command.id());
            current = null;
          }
          Thread.sleep(100);
        } catch (InterruptedException e) {
          isRunning = false;
        } catch (Exception e) {
          if (current != null) {
            commandQueue.fail(current.id());
          }
        }
      }
      log.info("command broker shut down.");
    }

    public void terminate() {
      isRunning = false;
    }

  }

}
