package net.snet.crm.infrastructure.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class SystemCommandRunner {
  private static final Logger log = LoggerFactory.getLogger(SystemCommandRunner.class);
  private static final ExecutorService executor = Executors.newSingleThreadExecutor();


  public static void executeSystemCommand(final SystemCommand command) {
    final Runnable task = new Runnable() {
      @Override
      public void run() {
        try {
          log.debug("executing system command '{}'...", command.name());
          command.run();
          log.info("system command '{}' completed", command.name());
        } catch (Exception e) {
          log.error("failed executing '{}'", command.name(), e);
        }
      }
    };
    final Future<?> future = executor.submit(task);
    try {
      future.get();
    } catch (InterruptedException e) {
      throw new RuntimeException(
          "system command '" + command.name() + "' has been interrupted", e);
    } catch (ExecutionException e) {
      throw new RuntimeException(
          "execution of system command '" + command.name() + "' failed", e);
    }
  }
}
