package cn.dreampie;

import cn.dreampie.CoffeeScriptCompiler;
import org.junit.Test;
import org.sonatype.plexus.build.incremental.ThreadBuildContext;

import java.io.File;

public class CoffeeScriptMojoTest {

  @Test
  public void testExecute() throws Exception {
    CoffeeScriptCompiler coffeeScriptCompiler = new CoffeeScriptCompiler();
    coffeeScriptCompiler.setBuildContext(ThreadBuildContext.getContext());
    File dir = new File(getClass().getResource("/").getPath());
    coffeeScriptCompiler.setSourceDirectory(dir);
    coffeeScriptCompiler.setOutputDirectory(dir);
    coffeeScriptCompiler.setCompress(true);
    coffeeScriptCompiler.setWatch(false);

    coffeeScriptCompiler.execute();
  }
}