package cn.dreampie;

import cn.dreampie.resource.CoffeeSource;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

/**
 * Created by wangrenhui on 2014/7/11.
 */
public class CoffeeScriptCompiler extends AbstractCoffeeScript {

  private Log log = LogKit.getLog();
  private CoffeeCompiler coffeeCompiler;
  /**
   * The directory for compiled javascript.
   */
  protected File outputDirectory;

  /**
   * When <code>true</code> the COFFEE compiler will compress the javascript.
   */
  private boolean compress;

  /**
   * When <code>true</code> the plugin will watch for changes in COFFEE files and compile if it detects one.
   */
  protected boolean watch = false;

  /**
   * When <code>true</code> the plugin will watch for changes in COFFEE files and compile if it detects one.
   */
  private int watchInterval = 1000;

  /**
   * The character encoding the COFFEE compiler will use for writing the javascript.
   */
  private String encoding;

  /**
   * When <code>true</code> forces the COFFEE compiler to always compile the coffeescript sources. By default coffeescript sources are only compiled when modified (including imports) or the CSS stylesheet does not exists.
   */
  private boolean force;

  /**
   * The location of the COFFEE JavasSript file.
   */
  private File coffeeJs;

  /**
   * The format of the output file names.
   */
  private String outputFileFormat;

  private static final String FILE_NAME_FORMAT_PARAMETER_REGEX = "\\{fileName\\}";

  private String[] args;

  private long lastErrorModified = 0;

  private boolean followDelete = false;

  private static final WatchEvent.Kind<?>[] watchEvents = {StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE};

  /**
   * Execute the MOJO.
   *
   * @throws CoffeeException if something unexpected occurs.
   */
  public void execute() {
    log.info("sourceDirectory = " + sourceDirectory);
    log.info("outputDirectory = " + outputDirectory);
    log.debug("includes = " + Arrays.toString(includes));
    log.debug("excludes = " + Arrays.toString(excludes));
    log.debug("force = " + force);
    log.debug("coffeeJs = " + coffeeJs);
    log.debug("skip = " + skip);

    if (!skip) {
      String[] files = getIncludedFiles();

      if (files == null || files.length < 1) {
        log.info("Nothing to compile - no coffee sources found");
      } else {
        if (log.isDebugEnabled()) {
          log.debug("included files = " + Arrays.toString(files));
        }

        Object coffeeCompiler = initCoffeeCompiler();
        compileIfChanged(files, coffeeCompiler);
        if (watch) {
          log.info("Watching " + sourceDirectory);
          if (force) {
            force = false;
            log.info("Disabled the 'force' flag in watch mode.");
          }
          startWatch(files, coffeeCompiler);
        }
      }
    } else {
      log.info("Skipping plugin execution per configuration");
    }
  }

  private void compileIfChanged(String[] files, Object coffeeCompiler) {

    for (String file : files) {
      compileIfChanged(coffeeCompiler, file);
    }

  }

  private void compileIfChanged(Object coffeeCompiler, String file) {
    File input = new File(sourceDirectory, file);

    buildContext.removeMessages(input);

    if (outputFileFormat != null) {
      file = outputFileFormat.replaceAll(FILE_NAME_FORMAT_PARAMETER_REGEX, file.replace(".coffee", ""));
    }

    String outFile = null;
    if (isCompress()) {
      outFile = file.replace(".coffee", ".min.js");
    } else {
      outFile = file.replace(".coffee", ".js");
    }

    File output = new File(outputDirectory, outFile);

    if (!output.getParentFile().exists() && !output.getParentFile().mkdirs()) {
      log.error("Cannot create output directory " + output.getParentFile());
      return;
    }

    try {
      CoffeeSource coffeeSource = new CoffeeSource(input);
      long coffeeLastModified = coffeeSource.getLastModified();
      if (!output.exists() || (force || output.lastModified() < coffeeLastModified) && lastErrorModified < coffeeLastModified) {
        lastErrorModified = coffeeLastModified;
        long compilationStarted = System.currentTimeMillis();
        log.info("Compiling coffee source: " + file);
        if (coffeeCompiler instanceof CoffeeCompiler) {
          ((CoffeeCompiler) coffeeCompiler).compile(coffeeSource, output, force);
        }
        buildContext.refresh(output);
        log.info("Finished compilation to " + outputDirectory + " in " + (System.currentTimeMillis() - compilationStarted) + " ms");
      } else if (!watch) {
        log.info("Bypassing coffee source: " + file + " (not modified)");
      }
    } catch (IOException e) {
//                buildContext.addMessage(input, 0, 0, "Error compiling coffee source", BuildContext.SEVERITY_ERROR, e);
      log.error("Error while compiling coffee source: " + file);
    } catch (CoffeeException e) {
      log.error("Error while compiling coffee source: " + file);
    }
  }

  private Object initCoffeeCompiler() throws CoffeeException {

    if (coffeeCompiler == null) {
      CoffeeCompiler newCoffeeCompiler = new CoffeeCompiler();
      newCoffeeCompiler.setCompress(compress);
      newCoffeeCompiler.setEncoding(encoding);
      newCoffeeCompiler.setOptionArgs(args);
      if (coffeeJs != null) {
        try {
          newCoffeeCompiler.setCoffeeJs(coffeeJs.toURI().toURL());
        } catch (MalformedURLException e) {
          throw new CoffeeException("Error while loading coffeescript: " + coffeeJs.getAbsolutePath(), e);
        }
      }
      coffeeCompiler = newCoffeeCompiler;
    }
    return coffeeCompiler;

  }

  private void startWatch(String[] files, Object compiler) {
    Path sourcePath = sourceDirectory.toPath();
    Path outPath = outputDirectory.toPath();
    WatchService watchService = null;
    try {
      watchService = initWatch(sourcePath);
    } catch (IOException e) {
      throw new CoffeeException("Error watch sourceDirectory: " + sourceDirectory.getAbsolutePath(), e);
    }
    boolean changed = true;
    while (true) {
      if (changed) {
        log.info("Waiting for changes...");
        changed = false;
      }

      WatchKey watchKey = null;
      try {
        watchKey = watchService.take();
      } catch (InterruptedException e) {
        log.error("Error get watch key", e);
      }
      Path dir = (Path) watchKey.watchable();

      for (WatchEvent<?> event : watchKey.pollEvents()) {
        Path file = dir.resolve((Path) event.context());
        log.debug(String.format("watched %s - %s", event.kind().name(), file));

        if (Files.isDirectory(file)) {
          if (event.kind().name().equals(StandardWatchEventKinds.ENTRY_CREATE.name())) {
            // watch created folder.
            try {
              file.register(watchService, watchEvents);
            } catch (IOException e) {
              log.error("Error register new folder", e);
            }
            log.debug(String.format("watch %s", file));
          }
          continue;
        }

        String fileName = sourcePath.relativize(file).toString();

        String outName = "";

        for (String name : files) {
          if (name != null && name.equals(fileName)) {

            if (isCompress()) {
              outName = fileName.replace(".coffee", ".min.js");
            } else {
              outName = fileName.replace(".coffee", ".js");
            }

            if (Files.exists(sourcePath.resolve(fileName)) && Files.notExists(outPath.resolve(outName))) {
              compileIfChanged(compiler, fileName);
            }

            if (event.kind().name().equals(StandardWatchEventKinds.ENTRY_DELETE.name())) {
              if (followDelete) {
                try {
                  if (Files.deleteIfExists(outPath.resolve(outName))) {
                    log.info(String.format("deleted %s with %s", outName, name));
                    changed = true;
                  }
                } catch (IOException e) {
                  log.error("Error delete file:" + outName, e);
                }
              }
            } else if (event.kind().name().equals(StandardWatchEventKinds.ENTRY_MODIFY.name()) || event.kind().name().equals(StandardWatchEventKinds.ENTRY_CREATE.name())) {
              compileIfChanged(compiler, fileName);
              changed = true;
            }
          }
        }
      }
      watchKey.reset();
    }

  }

  private WatchService initWatch(Path sourceDirectory) throws IOException {
    final WatchService watchService = sourceDirectory.getFileSystem().newWatchService();

    Files.walkFileTree(sourceDirectory, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        dir.register(watchService, watchEvents);
        log.debug(String.format("watch %s", dir));
        return FileVisitResult.CONTINUE;
      }
    });
    return watchService;
  }

  public File getOutputDirectory() {
    return outputDirectory;
  }

  public void setOutputDirectory(File outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public boolean isCompress() {
    return compress;
  }

  public void setCompress(boolean compress) {
    this.compress = compress;
  }

  public boolean isWatch() {
    return watch;
  }

  public void setWatch(boolean watch) {
    this.watch = watch;
  }

  public int getWatchInterval() {
    return watchInterval;
  }

  public void setWatchInterval(int watchInterval) {
    this.watchInterval = watchInterval;
  }

  public String getEncoding() {
    return encoding;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public boolean isForce() {
    return force;
  }

  public void setForce(boolean force) {
    this.force = force;
  }

  public File getCoffeeJs() {
    return coffeeJs;
  }

  public void setCoffeeJs(File coffeeJs) {
    this.coffeeJs = coffeeJs;
  }

  public String getOutputFileFormat() {
    return outputFileFormat;
  }

  public void setOutputFileFormat(String outputFileFormat) {
    this.outputFileFormat = outputFileFormat;
  }

  public void setArgs(String... args) {
    this.args = args;
  }

  public long getLastErrorModified() {
    return lastErrorModified;
  }

  public void setLastErrorModified(long lastErrorModified) {
    this.lastErrorModified = lastErrorModified;
  }

  public boolean isFollowDelete() {
    return followDelete;
  }

  public void setFollowDelete(boolean followDelete) {
    this.followDelete = followDelete;
  }
}