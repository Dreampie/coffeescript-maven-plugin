package cn.dreampie;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.sonatype.plexus.build.incremental.BuildContext;

import java.io.File;

/**
 * Created by ice on 14-11-17.
 * author Dreampie
 * phase generate-resources
 * goal compile
 */
public abstract class AbstractCoffeeScriptMojo extends AbstractMojo {

  protected Log log = getLog();
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
  @Parameter(defaultValue = "${project.build.directory}/javascript", required = true)
  protected File outputDirectory;

  /**
   * When <code>true</code> the coffee compiler will compress the javascript.
   */
  @Parameter(defaultValue = "false")
  protected boolean compress;

  /**
   * The character encoding the coffee compiler will use for writing the javascript.
   */
  @Parameter(defaultValue = "${project.build.sourceEncoding}")
  protected String encoding;

  /**
   * When <code>true</code> forces the coffee compiler to always compile the coffee sources. By default coffee sources are only compiled when modified (including imports) or the CSS stylesheet does not exists.
   */
  @Parameter(defaultValue = "false")
  protected boolean force;

  /**
   * The location of the coffee JavasSript file.
   */
  @Parameter
  protected File coffeeJs;

  /**
   * The location of the NodeJS executable.
   */
  @Parameter
  protected String nodeExecutable;

  /**
   * The format of the output file names.
   */
  @Parameter
  protected String outputFileFormat;

  /**
   * The compile args.
   */
  @Parameter
  protected String[] args;

  @Parameter(defaultValue = "false")
  protected boolean flowDelete;

  protected CoffeeScriptCompiler coffeeScriptCompiler;


  protected void initCompiler() {
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
//    coffeeScriptCompiler.setWatch(true);
    coffeeScriptCompiler.setNodeExecutable(nodeExecutable);
    coffeeScriptCompiler.setOutputFileFormat(outputFileFormat);
    coffeeScriptCompiler.setFollowDelete(flowDelete);
  }
}
