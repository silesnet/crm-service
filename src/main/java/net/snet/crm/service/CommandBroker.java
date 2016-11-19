package net.snet.crm.service;

import io.dropwizard.lifecycle.Managed;
import net.snet.crm.domain.shared.command.Command;
import net.snet.crm.domain.shared.command.CommandQueue;
import net.snet.crm.domain.shared.command.Commands;
import net.snet.crm.infrastructure.command.Task;
import net.snet.crm.infrastructure.command.TaskFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CommandBroker implements Managed {
  private static final Logger log = LoggerFactory.getLogger(CommandBroker.class);
  private final Thread thread;
  private final Broker broker;
  private final TaskFactory taskFactory;

  public CommandBroker(CommandQueue commandQueue, TaskFactory taskFactory) {
    broker = new Broker(commandQueue);
    this.taskFactory = taskFactory;
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
            log.debug("processing command '{}' for '{}/{}'...",
                command.name(), command.entity(), command.entityId());
            final Task task = taskFactory.of(command);
            commandQueue.process(command.id());
            task.perform();
            commandQueue.complete(command.id());
            current = null;
            log.debug("command '{}' for '{}/{}' completed",
                command.name(), command.entity(), command.entityId());
          }
          Thread.sleep(100);
        } catch (InterruptedException e) {
          isRunning = false;
        } catch (Exception e) {
          if (current != null) {
            log.error("failed processing command '{}', for '{}/{}'",
                current.name(), current.entity(), current.entityId());
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
