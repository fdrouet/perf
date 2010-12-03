package org.tarpoon.performance.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FakeService {
  private static final Logger LOGGER = LoggerFactory.getLogger(FakeService.class);

  /**
   * @param args
   */
  public static void main(String[] args) {
    FakeService fs = new FakeService();
    fs.call1();
    fs.call2();
    fs.call3("");
  }

  private void call1() {
    LOGGER.info("This methode does nothing ;-)");
  }

  private void call2() {
    LOGGER.info("This methode concatenate 5000 Strings with + operator");
    String s = "";
    for (int i = 0; i < 5000; i++) {
      s += i;
    }
  }

  private void call3(String fakeString) {
    LOGGER.info("This methode concatenate 5000 Strings with StringBuffer");
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < 5000; i++) {
      sb.append(i);
    }
  }

  private static void staticCall1() {
    LOGGER.info("This methode concatenate 5000 Strings with StringBuilder");
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 5000; i++) {
      sb.append(i);
    }
  }

}
