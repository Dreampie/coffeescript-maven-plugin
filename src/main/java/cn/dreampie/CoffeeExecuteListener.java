package cn.dreampie;

import org.apache.maven.plugin.logging.Log;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by wangrenhui on 2014/7/22.
 */
public class CoffeeExecuteListener implements Observer {

  private Log log = LogKit.getLog();

  private CoffeeExecuteThread coffeeExecuteThread;

  public CoffeeExecuteListener(CoffeeExecuteThread coffeeExecuteThread) {
    this.coffeeExecuteThread = coffeeExecuteThread;
  }

  public void update(Observable o, Object arg) {
    coffeeExecuteThread.addObserver(this);
    new Thread(coffeeExecuteThread).start();
    log.info("CoffeeExecuteThread is start");
  }
}
