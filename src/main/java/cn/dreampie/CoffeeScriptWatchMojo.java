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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ice on 14-11-17.
 * author Dreampie
 * phase generate-resources
 * goal compile
 */
// CHECKSTYLE_OFF: LineLength
@Mojo(name = "watch", threadSafe = true, defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
// CHECKSTYLE_ON: LineLength
public class CoffeeScriptWatchMojo extends AbstractCoffeeScriptMojo {

  /**
   * When <code>true</code> the plugin will watch for changes in coffee files and compile if it detects one.
   */
  @Parameter(defaultValue = "false")
  private boolean watchInThread;

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
    coffeeScriptCompiler.setWatch(true);
//    coffeeScriptCompiler.setWatchInterval(watchInterval);
    coffeeScriptCompiler.setNodeExecutable(nodeExecutable);
    coffeeScriptCompiler.setOutputFileFormat(outputFileFormat);
  }

  private void start() {
    if (watchInThread) {
      CoffeeExecuteThread thread = new CoffeeExecuteThread(coffeeScriptCompiler);
      CoffeeExecuteListener listen = new CoffeeExecuteListener(thread);
      thread.addObserver(listen);
      new Thread(thread).start();
//      Executors.newSingleThreadExecutor().execute(thread);
    } else
      coffeeScriptCompiler.execute();
  }

}
