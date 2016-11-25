package net.snet.crm.infrastructure.system;

import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public class FileSystemCommandFactory implements SystemCommandFactory {
  private final File homeFolder;

  public FileSystemCommandFactory(File homeFolder) {
    checkArgument(homeFolder.isDirectory() && homeFolder.exists(),
        "system command home is invalid '%s'", homeFolder);
    this.homeFolder = homeFolder;
  }

  @Override
  public SystemCommand systemCommand(final String name, final String... args) {
    final File script = script(candidates(name));
    checkArgument(script.canExecute(), "'%s' is not executable", script);
    final List<String> command = Lists.asList(script.getAbsolutePath(), args);
    return new SystemCommand() {

      @Override
      public String name() {
        return name;
      }

      @Override
      public void run() {
        try {
          Process process = new ProcessBuilder(command)
              .redirectErrorStream(true)
              .redirectOutput(ProcessBuilder.Redirect.INHERIT)
              .start();
            int error = process.waitFor();
            if (error != 0) {
              throw new RuntimeException("error code: " + error);
            }
        } catch (Exception e) {
          throw new RuntimeException("can't run '" + Joiner.on(" ").join(command) + "'", e);
        }
      }
    };
  }

  private File script(String... names) {
    for (String name : names) {
      final File file = new File(homeFolder, name);
      if (file.exists() && file.isFile()) {
        return file;
      }
    }
    throw new IllegalArgumentException("command script does not exist '[" +
        Joiner.on(", ").join(names) + "]' in '" + homeFolder + "'");
  }

  private String[] candidates(String name) {
    final List<String> names = Lists.newArrayList();
    names.add(name);
    names.add(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, name));
    names.add(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name));
    final List<String> namesWithExtension = Lists.newArrayList();
    for (String candidate : names) {
      namesWithExtension.add(candidate + ".sh");
      namesWithExtension.add(candidate + ".cmd");
    }
    names.addAll(namesWithExtension);
    return Iterables.toArray(names, String.class);
  }
}
