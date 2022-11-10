package com.cez.terraform.api.v1.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.cez.terraform.api.v1.request.JSONOM;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Executor
{
  public static final Logger logger = LoggerFactory.getLogger(Executor.class);

  // public static String execute(String cmd, Map<String, String> environmentVars) throws IOException,
  // InterruptedException
  public static String execute(String cmd) throws IOException, InterruptedException
  {

    JSONOM mapper = new JSONOM();
    String output = null;

    Map<String, String> environmentVars = new HashMap<>();
    environmentVars = System.getenv();

    logger.debug("Execution Command Equivalent: " + cmd);
    StringBuffer outputValid = new StringBuffer();
    StringBuffer outputWarning = new StringBuffer();
    StringBuffer outputError = new StringBuffer();

    ByteArrayOutputStream stdoutStream = new ByteArrayOutputStream();
    ByteArrayOutputStream stderrStream = new ByteArrayOutputStream();
    DefaultExecutor executor = new DefaultExecutor();
    DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
    CommandLine commands = CommandLine.parse(cmd.toString());
    // timeout 1 minute
    Long timeout = Long.valueOf(60000);
    ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout);
    PumpStreamHandler streamHander = new PumpStreamHandler(stdoutStream, stderrStream);
    executor.setStreamHandler(streamHander);
    executor.setExitValue(1);
    executor.setWatchdog(watchdog);
    // executor.execute(commands, resultHandler);
    logger.debug("Environment Variables included during execution: " + environmentVars);
    logger.debug("Command Executed: " + cmd.toString());
    executor.execute(commands, environmentVars, resultHandler);
    resultHandler.waitFor();
    int returnValue = resultHandler.getExitValue();
    if (returnValue == 1) logger.info("Execution encountered error, returned {" + returnValue + "}. See JSON response for more details");
    if (returnValue == 0) logger.info("Execution successful, returned {" + returnValue + "}");
    String stdOut = stdoutStream.toString();
    String stdErr = stderrStream.toString();

    if (stdOut.length() > 5)
    {
      outputValid.append(stdOut);
      outputValid.append(System.lineSeparator());
      output = stdOut;
      logger.debug("Encoded Output: " + Base64.getEncoder().encodeToString(stdOut.getBytes()));
    }

    if (stdErr.length() > 5)
    {
      output = stdErr;
      if (stdErr.toLowerCase().startsWith("warn"))
      {
        outputWarning.append(stdErr);
        logger.debug("Encoded Warning: " + Base64.getEncoder().encodeToString(stdErr.getBytes()));
      }
      else
      {
        outputError.append(stdErr);
        logger.debug("Encoded Error: " + Base64.getEncoder().encodeToString(stdErr.getBytes()));
      }
    }

    String encodedOutput = Base64.getEncoder().encodeToString(outputValid.toString().getBytes());
    String encodedWarning = Base64.getEncoder().encodeToString(outputWarning.toString().getBytes());
    String encodedError = Base64.getEncoder().encodeToString(outputError.toString().getBytes());

    ObjectNode outputNode = mapper.createObjectNode();
    outputNode.putPOJO("Result", encodedOutput);
    outputNode.putPOJO("Warning", encodedWarning);
    outputNode.putPOJO("Error", encodedError);

    // return outputNode;

    return output;
  }

}
