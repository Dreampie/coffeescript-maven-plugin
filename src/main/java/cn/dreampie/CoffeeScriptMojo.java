package cn.dreampie;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.*;
import org.sonatype.plexus.build.incremental.BuildContext;

import java.io.File;

/**
 * Created by ice on 14-11-17.
 * author Dreampie
 * phase generate-resources
 * goal compile
 */
// CHECKSTYLE_OFF: LineLength
@Mojo(name = "compile", threadSafe = true, defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
// CHECKSTYLE_ON: LineLength
public class CoffeeScriptMojo extends AbstractMojo {

  private Log log = getLog();
  /**
   * component
   */
  @Component
  protected BuildContext buildContext;

  /**
   * The source directory containing the coffee sources.
   */
  @Parameter(defaultValue = "${project.basedir}/src/main/coffeescript")
  protected File sourceDirectory;

  /**
   * List of files to include. Specified as fileset patterns which are relative to the source directory. Default value is: { "**\/*.coffee" }
   */
  @Parameter
  protected String[] includes = new String[]{"**/*.coffee"};

  /**
   * List of files to exclude. Specified as fileset patterns which are relative to the source directory.
   */
  @Parameter
  protected String[] excludes = new String[]{};

  /**
   * Whether to skip plugin execution.
   * This makes the build more controllable from profiles.
   */
  @Parameter(defaultValue = "false")
  protected boolean skip;

  /**
   * The directory for compiled javascript.
   */
  @Parameter(defaultValue = "${project.build.directory}", required = true)
  protected File outputDirectory;

  /**
   * When <code>true</code> the coffee compiler will compress the javascript.
   */
  @Parameter(defaultValue = "false")
  private boolean compress;

  /**
   * When <code>true</code> the plugin will watch for changes in coffee files and compile if it detects one.
   */
  @Parameter(defaultValue = "false")
  protected boolean watch;

  /**
   * When <code>true</code> the plugin will watch for changes in coffee files and compile if it detects one.
   */
  @Parameter(defaultValue = "1000")
  private int watchInterval;

  /**
   * The character encoding the coffee compiler will use for writing the javascript.
   */
  @Parameter(defaultValue = "${project.build.sourceEncoding}")
  private String encoding;

  /**
   * When <code>true</code> forces the coffee compiler to always compile the coffee sources. By default coffee sources are only compiled when modified (including imports) or the CSS stylesheet does not exists.
   */
  @Parameter(defaultValue = "false")
  private boolean force;

  /**
   * The location of the coffee JavasSript file.
   */
  @Parameter
  private File coffeeJs;

  /**
   * The location of the NodeJS executable.
   */
  @Parameter
  private String nodeExecutable;

  /**
   * The format of the output file names.
   */
  @Parameter
  private String outputFileFormat;

  /**
   * The compile args.
   */
  @Parameter
  private String[] args;

  /**
   * The restart thread time.
   */
  @Parameter(defaultValue = "1000")
  private int restartInterval;

  private CoffeeScriptCompiler coffeeScriptCompiler;

  public void execute() throws MojoExecutionException, MojoFailureException {
    LogKit.setLog(log);
    initCompiler();
    start();
  }

  private void initCompiler() {
    coffeeScriptCompiler = new CoffeeScriptCompiler();
    coffeeScriptCompiler.setBuildContext(buildContext);
    coffeeScriptCompiler.setIncludes(includes);
    coffeeScriptCompiler.setExcludes(excludes);
    coffeeScriptCompiler.setCoffeeJs(coffeeJs);
    coffeeScriptCompiler.setSkip(skip);
    coffeeScriptCompiler.setSourceDirectory(sourceDirectory);
    coffeeScriptCompiler.setOutputDirectory(outputDirectory);
    coffeeScriptCompiler.setForce(force);
    coffeeScriptCompiler.setEncoding(encoding);
    coffeeScriptCompiler.setCompress(compress);
    coffeeScriptCompiler.setArgs(args);
    coffeeScriptCompiler.setWatch(watch);
    coffeeScriptCompiler.setWatchInterval(watchInterval);
    coffeeScriptCompiler.setNodeExecutable(nodeExecutable);
    coffeeScriptCompiler.setOutputFileFormat(outputFileFormat);
  }

  private void start() {
    if (watch) {
      CoffeeExecuteThread thread = new CoffeeExecuteThread(coffeeScriptCompiler, restartInterval);
      CoffeeExecuteListener listen = new CoffeeExecuteListener(thread);
      thread.addObserver(listen);
      new Thread(thread).start();
    } else {
      coffeeScriptCompiler.execute();
    }
  }

}
