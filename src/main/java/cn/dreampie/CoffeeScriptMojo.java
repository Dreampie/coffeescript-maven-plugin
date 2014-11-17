package cn.dreampie;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
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

  /**
   * component
   */
  @Component
  protected BuildContext buildContext;

  /**
   * The source directory containing the LESS sources.
   * <p/>
   * parameter default-value="${project.basedir}/src/main/lesscss"
   * required
   */
  @Parameter(defaultValue = "${project.basedir}/src/main/lesscss")
  protected File sourceDirectory;

  /**
   * List of files to include. Specified as fileset patterns which are relative to the source directory. Default value is: { "**\/*.less" }
   * <p/>
   * parameter
   */
  @Parameter
  protected String[] includes = new String[]{"**/*.less"};

  /**
   * List of files to exclude. Specified as fileset patterns which are relative to the source directory.
   * <p/>
   * parameter
   */
  @Parameter
  protected String[] excludes = new String[]{};

  /**
   * Whether to skip plugin execution.
   * This makes the build more controllable from profiles.
   * <p/>
   * parameter default-value="false"
   */
  @Parameter(defaultValue = "false")
  protected boolean skip;

  /**
   * The directory for compiled javascript.
   * <p/>
   * parameter default-value="${project.build.directory}"
   * required
   */
  @Parameter(defaultValue = "${project.build.directory}", required = true)
  protected File outputDirectory;

  /**
   * When <code>true</code> the COFFEE compiler will compress the javascript.
   * <p/>
   * parameter default-value="false"
   */
  @Parameter(defaultValue = "false")
  private boolean compress;

  /**
   * When <code>true</code> the plugin will watch for changes in COFFEE files and compile if it detects one.
   * <p/>
   * parameter default-value="false"
   */
  @Parameter(defaultValue = "false")
  protected boolean watch;

  /**
   * When <code>true</code> the plugin will watch for changes in COFFEE files and compile if it detects one.
   * <p/>
   * parameter default-value="1000"
   */
  @Parameter(defaultValue = "1000")
  private int watchInterval;

  /**
   * The character encoding the COFFEE compiler will use for writing the javascript.
   * <p/>
   * parameter default-value="${project.build.sourceEncoding}"
   */
  @Parameter(defaultValue = "${project.build.sourceEncoding}")
  private String encoding;

  /**
   * When <code>true</code> forces the COFFEE compiler to always compile the COFFEE sources. By default COFFEE sources are only compiled when modified (including imports) or the CSS stylesheet does not exists.
   * <p/>
   * parameter default-value="false"
   */
  @Parameter(defaultValue = "false")
  private boolean forcing;

  /**
   * The location of the COFFEE JavasSript file.
   * <p/>
   * parameter
   */
  @Parameter
  private File coffeeJs;

  /**
   * The location of the NodeJS executable.
   * <p/>
   * parameter
   */
  @Parameter
  private String nodeExecutable;

  /**
   * The format of the output file names.
   * <p/>
   * parameter
   */
  @Parameter
  private String outputFileFormat;

  /**
   * The compile args.
   * <p/>
   * parameter
   */
  @Parameter
  private String[] args;

  /**
   * The restart thread time.
   * <p/>
   * parameter
   */
  @Parameter(defaultValue = "1000")
  private int restartInterval;

  private CoffeeScriptCompiler coffeeScriptCompiler;

  public void execute() throws MojoExecutionException, MojoFailureException {
    LogKit.setLog(getLog());
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
    coffeeScriptCompiler.setForce(forcing);
    coffeeScriptCompiler.setEncoding(encoding);
    coffeeScriptCompiler.setCompress(compress);
    coffeeScriptCompiler.setArgs(args);
    coffeeScriptCompiler.setWatch(watch);
    coffeeScriptCompiler.setWatchInterval(watchInterval);
    coffeeScriptCompiler.setNodeExecutable(nodeExecutable);
    coffeeScriptCompiler.setOutputFileFormat(outputFileFormat);
  }

  private void start() {
    CoffeeExecuteThread run = new CoffeeExecuteThread(coffeeScriptCompiler, restartInterval);
    CoffeeExecuteListener listen = new CoffeeExecuteListener(run);
    run.addObserver(listen);
    new Thread(run).start();
  }

}
