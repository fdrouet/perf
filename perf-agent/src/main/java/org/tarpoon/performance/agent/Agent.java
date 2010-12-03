/**
 * 
 */
package org.tarpoon.performance.agent;

import java.lang.instrument.Instrumentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JVM Agent used to inject ClassFileTransformer and modify the Bytecode.
 * 
 * @author fdrouet
 */
public class Agent {

  private static final Logger LOGGER = LoggerFactory.getLogger(Agent.class);

  public static void premain(String agentArgs, Instrumentation inst) {

    LOGGER.info("*** Premain method is called : loading " + TimerTransformer.class.getSimpleName());
    inst.addTransformer(new TimerTransformer(), true);
  }

  public static void premain(String agentArgs) {
    LOGGER.error("Will not be called because of the premain(String, Intrusrumentation) method.");
  }

  public static void agentmain(String agentArgs, Instrumentation inst) {
    LOGGER.error("* Agent main : used if the agent is added at runtime");
    // inst.addTransformer(new PrintTransformer(), true);
  }
}
