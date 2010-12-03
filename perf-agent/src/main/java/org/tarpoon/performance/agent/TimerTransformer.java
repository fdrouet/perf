package org.tarpoon.performance.agent;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author fdrouet
 */
public class TimerTransformer implements ClassFileTransformer {

  private static final Logger LOGGER                  = LoggerFactory.getLogger(TimerTransformer.class);

  private static final String SUFFIX_TIMER            = "$timer";

  private static final String FIELD_NAME_AGENT_LOGGER = "AGENT_LOGGER";

  public byte[] transform(ClassLoader loader,
                          String _className,
                          Class<?> classBeingRedefined,
                          ProtectionDomain protectionDomain,
                          byte[] classfileBuffer) throws IllegalClassFormatException {

    String className = _className.replaceAll("/", ".");
    byte[] modifiedClass = null;

    if (className.startsWith("org.tarpoon")) {
      LOGGER.info("++ Class name to instrument : " + className);
      CtClass clas = ClassPool.getDefault().getOrNull(className);

      if (clas == null) {
        LOGGER.error("!!!!! Impossible d'instrumenter la classe " + className);
      } else {
        LOGGER.info("Tentative d'instrumentation avec " + clas.getClass().getName());
        try {
          clas = addAgentLogger(clas);
          clas = addTiming(clas, "staticCall1");
          clas = addTimingForMethodFilter(clas, "call.*");
          // clas = addTiming(clas, "call1");
          // clas = addTiming(clas, "call2");
          // clas = addTiming(clas, "call3");
          LOGGER.info("Class enrichie");
          modifiedClass = clas.toBytecode();
          LOGGER.info("ByteCode récupéré");
          clas.detach();
          LOGGER.info("Class detached");
        } catch (IOException e) {
          LOGGER.error("Impossible d'instrumenter la classe " + className, e);
        } catch (CannotCompileException e) {
          LOGGER.error("Impossible de compiler l'instrumentation de la classe " + className, e);
        } catch (NotFoundException e) {
          LOGGER.error("Impossible de trouver la methode call1 de la classe " + className, e);
        } catch (Exception e) {
          LOGGER.error("Impossible d'instrumenter la classe " + className, e);
        }
      }

    } else {
      LOGGER.debug("-- Class name to not instrument : " + className);
    }
    return modifiedClass;
  }

  /**
   * This methode add a logger in the class.
   * 
   * @param clas the class on which we have to add the AGENT_LOGGER
   * @return the modified class
   * @throws CannotCompileException
   * @throws NotFoundException
   */
  private CtClass addAgentLogger(CtClass clas) throws CannotCompileException, NotFoundException {
    LOGGER.info("Adding AGENT_LOGGER to the class " + clas.getName());

    CtClass LoggerClass = ClassPool.getDefault().get(Logger.class.getName());
    CtField loggerField = new CtField(LoggerClass, FIELD_NAME_AGENT_LOGGER, clas);
    loggerField.setModifiers(AccessFlag.PRIVATE | AccessFlag.STATIC | AccessFlag.FINAL);
    clas.addField(loggerField, LoggerFactory.class.getName() + ".getLogger(" + clas.getName() + ".class);");

    LOGGER.debug(loggerField.toString());
    ClassPool.getDefault().get(Logger.class.getName()).detach();
    return clas;
  }

  /**
   * Add a timer on a named method (without the parameters)
   * 
   * @param clas the class to instrument
   * @param methodName the name of the method without it's parameters
   * @return the modified class
   * @throws NotFoundException
   * @throws CannotCompileException
   */
  private CtClass addTiming(CtClass clas, String methodName) throws NotFoundException, CannotCompileException {
    LOGGER.info("Instrumentation of method : " + methodName);

    CtMethod method = clas.getDeclaredMethod(methodName);
    addTiming(clas, method);

    return clas;
  }

  /**
   * Add a timer on the methods which match the methodNameFilter regexp (without
   * the parameters)
   * 
   * @param clas the class to instrument
   * @param methodNameFilter the regexp corresponding to the methods without
   *          it's parameters
   * @return the modified class
   * @throws NotFoundException
   * @throws CannotCompileException
   */
  private CtClass addTimingForMethodFilter(CtClass clas, String methodNameFilter) throws NotFoundException, CannotCompileException {
    CtMethod[] methods = clas.getDeclaredMethods();
    for (CtMethod method : methods) {
      LOGGER.info(method.getName());
      if (method.getName().matches(methodNameFilter)) {
        LOGGER.info("addTiming(" + clas.getName() + ", " + method.getName() + ")");
        clas = addTiming(clas, method);
      } else {
        LOGGER.info("la methode [" + method.getName() + "] ne correspond pas au filtre [" + methodNameFilter + "]");
      }
    }

    return clas;
  }

  /**
   * Add a timer on a specified method
   * 
   * @param clas the class to instrument
   * @param method the method on which we want to add a timer
   * @return the modified class
   * @throws NotFoundException
   * @throws CannotCompileException
   */
  private CtClass addTiming(CtClass clas, CtMethod method) throws NotFoundException, CannotCompileException {
    String methodName = method.getName();
    String methodTimerName = methodName + SUFFIX_TIMER;

    method.setName(methodTimerName);
    CtMethod newMethod = CtNewMethod.copy(method, methodName, clas, null);
    String returnType = method.getReturnType().getName();
    StringBuffer body = new StringBuffer();
    body.append("{\nlong start = System.currentTimeMillis();\n");
    if (!"void".equals(returnType)) {
      body.append(returnType + " result = ");
    }
    body.append(methodTimerName + "($$);\n");
    // TODO output the timer data in another place than logging system to be
    // able to easily compute it later
    body.append(FIELD_NAME_AGENT_LOGGER + ".info(\"" + newMethod.getLongName() + " = \" +\n (System.currentTimeMillis()-start) + " + "\" ms.\");\n");
    if (!"void".equals(returnType)) {
      body.append("return result;\n");
    }
    body.append("}");
    newMethod.setBody(body.toString());
    clas.addMethod(newMethod);
    return clas;
  }

}
