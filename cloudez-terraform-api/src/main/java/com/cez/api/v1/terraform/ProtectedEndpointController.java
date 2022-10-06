package com.cez.api.v1.terraform;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cez.api.v1.terraform.request.JSONOM;
import com.cez.api.v1.terraform.request.JSONRequest;
import com.cez.api.v1.terraform.request.JSONResponse;
import com.cez.api.v1.terraform.request.RequestObject;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
@RequestMapping("terraform/protected/api/v1")
public class ProtectedEndpointController
{
  public static final Logger logger = LoggerFactory.getLogger(ProtectedEndpointController.class);

  @Autowired
  BuildProperties build;

  @Value("${terraform.executable}")
  private String terraform;

  @Value("${terraform.data.home}")
  private String tfDataDir;

  private String output;

  private JSONOM mapper = null;
  private ObjectNode parentNode = null;
  private JSONRequest jsonRequest = null;
  private JSONResponse jsonResponse = null;

  @PostMapping("/execute")
  public String execute(@RequestBody String requestBody)
  {
    mapper = new JSONOM();
    parentNode = mapper.createObjectNode();
    ObjectNode section = mapper.createObjectNode();

    String response = null;
    // String tfDataDir = "/Users/sv/terraform-data";
    String execDir = tfDataDir + "/" + "exec" + new Date().getTime();

    setEnv("TF_DATA_DIR", execDir);
    Map<String, String> environmentVars = new HashMap<>();
    environmentVars = System.getenv();

    jsonRequest = new JSONRequest(requestBody);
    jsonResponse = new JSONResponse();
    RequestObject request = (RequestObject) jsonRequest.getContent();
    try
    {
      File tfDirectory = new File(execDir);
      Boolean isDirectory = tfDirectory.mkdir();
      List<String> logicalResources = new ArrayList<>();

      if (isDirectory)
      {
        // System.out.println(System.getenv("TF_DATA_DIR"));
        if (jsonRequest.getOperation().equalsIgnoreCase("apply"))
        {
          // create main.tf
          File mainTF = new File(execDir + "/main.tf");
          FileWriter fileWriter = new FileWriter(mainTF);
          fileWriter.write(request.getScript());
          fileWriter.close();
          // copy backend.tf
          File backendTF = new File(execDir + "/backend.tf");
          org.apache.commons.io.FileUtils.copyFile(new File("terraform-resources/backend.tf"), backendTF);
          if (mainTF.exists())
          {
            response = execute(terraform + " -chdir=" + execDir + " -lock=false " + " init", environmentVars);
            logger.debug("Init Console Out :: " + response);
            response = execute(terraform + " -chdir=" + execDir + " -lock=false " + " apply -auto-approve", environmentVars);
            logger.debug("Apply Console Out :: " + response);
            execute(terraform + " -chdir=" + execDir + " -lock=false " + " init", environmentVars);
            String out = execute(terraform + " -chdir=" + execDir + " state list", environmentVars);
            StringTokenizer tokenizer = new StringTokenizer(out);
            while (tokenizer.hasMoreTokens())
            {
              // System.out.println(tokenizer.nextToken());
              logicalResources.add(tokenizer.nextToken());
            }
          }
        }
        FileUtils.deleteDirectory(tfDirectory);
        section.putPOJO("output", new String(Base64Utils.encode(response.getBytes())));
        section.putPOJO("logical-resource-name", logicalResources);
      }
      // output = jsonResponse.getResponse(parentNode.putPOJO("terraform", new
      // String(Base64Utils.encode(response.getBytes()))));
      output = jsonResponse.getResponse(parentNode.putPOJO("terraform", section));
    }
    catch (IOException | InterruptedException e)
    {
      e.printStackTrace();
    }
    return output;
  }

  @DeleteMapping("/destroy")
  public String destroy(@PathVariable String requestBody)
  {
    mapper = new JSONOM();
    parentNode = mapper.createObjectNode();

    String response = null;
    String tfDataDir = "/Users/sv/terraform-data";
    String execDir = tfDataDir + "/" + "exec" + new Date().getTime();

    setEnv("TF_DATA_DIR", execDir);
    Map<String, String> environmentVars = new HashMap<>();
    environmentVars = System.getenv();

    jsonRequest = new JSONRequest(requestBody);
    jsonResponse = new JSONResponse();
    RequestObject request = (RequestObject) jsonRequest.getContent();
    try
    {
      File tfDirectory = new File(execDir);
      Boolean isDirectory = tfDirectory.mkdir();

      if (isDirectory)
      {
        if (jsonRequest.getOperation().equalsIgnoreCase("destroy"))
        {
          // copy backend.tf
          File backendTF = new File(execDir + "/backend.tf");
          org.apache.commons.io.FileUtils.copyFile(new File("terraform-resources/backend.tf"), backendTF);

          System.out.println(request.getResource());

          execute(terraform + " -chdir=" + execDir + " init", environmentVars);
          // response = execute(terraform + " -chdir=" + System.getenv("TF_DATA_DIR") + " apply -destroy
          // --target " + request.getResource(), environmentVars);
        }
      }
      FileUtils.deleteDirectory(tfDirectory);
      output = jsonResponse.getResponse(parentNode.putPOJO("terraform", new String(Base64Utils.encode(response.getBytes()))));
    }
    catch (IOException | InterruptedException e)
    {
      e.printStackTrace();
    }
    return output;
  }

  private String execute(String cmd, Map<String, String> environmentVars) throws IOException, InterruptedException
  {
    mapper = new JSONOM();
    String output = null;

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

  private void setEnv(String key, String value)
  {
    try
    {
      Map<String, String> env = System.getenv();
      Class<?> cl = env.getClass();
      Field field = cl.getDeclaredField("m");
      field.setAccessible(true);
      Map<String, String> writableEnv = (Map<String, String>) field.get(env);
      writableEnv.put(key, value);
    }
    catch (Exception e)
    {
      throw new IllegalStateException("Failed to set environment variable", e);
    }
  }
}
