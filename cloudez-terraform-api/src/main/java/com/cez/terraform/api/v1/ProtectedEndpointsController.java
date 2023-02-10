package com.cez.terraform.api.v1;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import com.cez.terraform.api.v1.async.TerraformApply;
import com.cez.terraform.api.v1.request.JSONOM;
import com.cez.terraform.api.v1.request.JSONRequest;
import com.cez.terraform.api.v1.request.JSONResponse;
import com.cez.terraform.api.v1.request.RequestObject;
import com.cez.terraform.api.v1.utils.Executor;
import com.cez.terraform.api.v1.utils.TaskIdGenerator;
import com.cez.terraform.api.v1.utils.WorkspaceUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
@RequestMapping("terraform/protected/api/v1")
public class ProtectedEndpointsController
{
  public static final Logger logger = LoggerFactory.getLogger(ProtectedEndpointsController.class);

  @Autowired
  BuildProperties build;

  @Value("${terraform.executable}")
  private String terraform;

  @Value("${terraform.data.home}")
  private String tfDataDir;

  @Autowired
  private ScriptsRepository scriptRepo;

  @Autowired
  private DeploymentsRepository deploymentRepo;

  private String output;

  private JSONOM mapper = null;
  private ObjectNode parentNode = null;
  private JSONRequest jsonRequest = null;
  private JSONResponse jsonResponse = null;

  public ProtectedEndpointsController()
  {
    mapper = new JSONOM();
  }

  @GetMapping("/script")
  public String scriptList(@RequestParam String owner)
  {
    String response = null;
    ObjectNode parentNode = mapper.createObjectNode();
    List<Scripts> scriptsList = scriptRepo.findAll();
    Iterator<Scripts> iterator = scriptsList.iterator();
    ArrayNode scripts = mapper.createArrayNode();
    while (iterator.hasNext())
    {
      Scripts script = iterator.next();
      if (script.getDeploymentOwner().equalsIgnoreCase(owner));
      {
        ObjectNode content = mapper.createObjectNode();
        // content.putPOJO("encoded-script", script.getTfScript());
        content.putPOJO("encoded-script", script.getEncodedScript());
        content.putPOJO("owner", script.getDeploymentOwner());
        content.putPOJO("name", script.getDeploymentName());
        content.putPOJO("status", script.getDeploymentStatus());
        content.putPOJO("description", script.getDeploymentDescription());
        content.putPOJO("created", script.getCreatedOn());
        scripts.add(content);
      }
    }
    parentNode.putPOJO("scripts", scripts);
    jsonResponse = new JSONResponse();
    try
    {
      response = jsonResponse.getResponse(parentNode);
    }
    catch (JsonProcessingException e)
    {
      e.printStackTrace();
    }

    return response;
  }

  @PostMapping("/script")
  public String scriptStore(@RequestBody String requestBody)
  {
    String response = null;
    jsonRequest = new JSONRequest(requestBody);
    RequestObject request = (RequestObject) jsonRequest.getContent();
    if (scriptRepo.findAllById(Collections.singleton(jsonRequest.getDeploymentName())).size() < 1)
    {
      Scripts script = new Scripts();
      script.setTfScript(request.getEncodedScript());
      script.setAccount(jsonRequest.getAccount());
      script.setRegion(jsonRequest.getRegion());
      script.setDeploymentName(jsonRequest.getDeploymentName());
      script.setDeploymentDescription(jsonRequest.getDeploymentDescription());
      script.setDeploymentOwner(jsonRequest.getDeploymentOwner());
      script.setWorkspace(jsonRequest.getDeploymentName() + "-" + jsonRequest.getDeploymentOwner());
      script.setDeploymentStatus("not-executed");
      scriptRepo.save(script);
      response = jsonRequest.getDeploymentName() + " deployment stored";
    }
    else
    {
      response = "deployment exits";
    }
    return response;
  }

  @PutMapping("/script")
  public String scriptUpdate(@RequestBody String requestBody)
  {
    jsonRequest = new JSONRequest(requestBody);
    String response = jsonRequest.getDeploymentName() + " not found";
    RequestObject request = (RequestObject) jsonRequest.getContent();
    Scripts script = null;
    List<Scripts> list = scriptRepo.findAllById(Collections.singleton(jsonRequest.getDeploymentName()));
    Iterator<Scripts> iterator = list.iterator();
    while (iterator.hasNext())
    {
      script = iterator.next();
      script.setTfScript(request.getEncodedScript());
      script.setAccount(jsonRequest.getAccount());
      script.setRegion(jsonRequest.getRegion());
      script.setDeploymentDescription(jsonRequest.getDeploymentDescription());
      scriptRepo.save(script);
      response = jsonRequest.getDeploymentName() + " updated";
    }
    return response;
  }

  @DeleteMapping("/script")
  public String scriptDelete(@RequestParam String id)
  {
    String response = id + " not found";
    List<Scripts> list = scriptRepo.findAllById(Collections.singleton(id));
    Iterator<Scripts> iterator = list.iterator();
    while (iterator.hasNext())
    {
      Scripts script = iterator.next();
      scriptRepo.delete(script);
      response = "deleted";
    }

    return response;
  }

  @PostMapping("/execute")
  // public String execute(@RequestBody String requestBody)
  public @ResponseBody String execute(@RequestBody String requestBody)
  {
    parentNode = mapper.createObjectNode();
    ObjectNode section = mapper.createObjectNode();

    String response = null;
    setEnv("TF_DATA_DIR", tfDataDir);

    List<String> logicalResources = new ArrayList<>();

    jsonRequest = new JSONRequest(requestBody);
    jsonResponse = new JSONResponse();
    RequestObject request = (RequestObject) jsonRequest.getContent();
    try
    {
      Optional<Scripts> optionalScript = scriptRepo.findById(request.getId());
      if (optionalScript.isPresent())
      {
        Scripts script = optionalScript.get();

        WorkspaceUtils workspace = new WorkspaceUtils(terraform, tfDataDir);
        List<String> workspaces = workspace.list();
        if (workspaces.contains(script.getWorkspace()))
        {
          logger.debug("selecting existing workspace " + script.getWorkspace());
          workspace.select(script.getWorkspace());
        }
        else
        {
          logger.debug("creating workspace " + script.getWorkspace());
          workspace.create(script.getWorkspace());
        }

        File mainTF = new File(tfDataDir + "/main.tf");
        FileWriter fileWriter = new FileWriter(mainTF);
        fileWriter.write(script.getTfScript());
        fileWriter.close();
        if (mainTF.exists())
        {
          response = Executor.execute(terraform + " -chdir=" + tfDataDir + " apply -auto-approve");
          System.out.println(response);
          String out = Executor.execute(terraform + " -chdir=" + tfDataDir + " state list");
          System.out.println(out);
          StringTokenizer tokenizer = new StringTokenizer(out);
          while (tokenizer.hasMoreTokens())
          {
            logicalResources.add(tokenizer.nextToken());
          }
        }
        section.putPOJO("output", new String(Base64Utils.encode(response.getBytes())));
        section.putPOJO("logical-resource-name", logicalResources);
        output = jsonResponse.getResponse(parentNode.putPOJO("terraform", section));
      }
    }
    catch (IOException | InterruptedException e)
    {
      e.printStackTrace();
    }

    return output;
  }

  @PostMapping("/apply")
  public @ResponsePayload String apply(@RequestBody String requestBody)
  {
    setEnv("TF_DATA_DIR", tfDataDir);
    String taskId = TaskIdGenerator.getId();
    TerraformApply runner = new TerraformApply(requestBody, tfDataDir, terraform, taskId, scriptRepo, deploymentRepo);

    new Thread(runner).start();

    jsonResponse = new JSONResponse();
    parentNode = mapper.createObjectNode();
    parentNode.putPOJO("executionid", taskId);
    try
    {
      output = jsonResponse.getResponse(parentNode);
    }
    catch (JsonProcessingException e)
    {
      e.printStackTrace();
    }

    return output;
  }

  @GetMapping("/get-execution")
  public @ResponsePayload String getExecution(@RequestParam(name = "taskid") String taskId)
  {
    String output = null;
    jsonResponse = new JSONResponse();

    Optional<Deployments> executionOptional = deploymentRepo.findById(taskId);
    Deployments execution = executionOptional.orElseGet(null);
    ObjectNode parentNode = mapper.createObjectNode();
    parentNode.putPOJO("id", execution.getId());
    parentNode.putPOJO("status", execution.getStatus());
    parentNode.putPOJO("console-output", execution.getConsoleOutput());
    parentNode.putPOJO("executed-by", execution.getExecutedBy());
    parentNode.putPOJO("execution-output", execution.getExecutionOutput());
    parentNode.putPOJO("logical-resources", execution.getLogicalResources());
    parentNode.putPOJO("script-name", execution.getScriptId());
    parentNode.putPOJO("execution-response", execution.getExecutionResponse());
    parentNode.putPOJO("execution-timestamp", execution.getCreatedOn());
    try
    {
      output = jsonResponse.getResponse(parentNode);
    }
    catch (JsonProcessingException e)
    {
      e.printStackTrace();
    }

    return output;
  }

  @SuppressWarnings("unchecked")
  @GetMapping("/search")
  public @ResponsePayload String search(@RequestParam(name = "keyword") String keyword, @RequestParam(name = "owner") String owner)
  {
    String output = null;
    List<String> SEARCHABLE_FIELDS = Arrays.asList("deploymentName", "deploymentDescription");
    jsonResponse = new JSONResponse();
    parentNode = mapper.createObjectNode();

    try
    {
      List<Scripts> searchList = scriptRepo.searchBy(keyword, 5, SEARCHABLE_FIELDS.toArray(new String[0]));
      List<HashMap<String, String>> reducedList = new ArrayList<>();

      searchList.forEach(script -> {
        if (script.getDeploymentOwner().equalsIgnoreCase(owner))
        {
          HashMap<String, String> map = new HashMap<>();
          map.put("encoded-script", Base64.getEncoder().encodeToString(script.getTfScript().getBytes()));
          map.put("name", script.getDeploymentName());
          map.put("description", script.getDeploymentDescription());
          map.put("status", script.getDeploymentStatus());
          map.put("created", script.getCreatedOn().toString());
          map.put("owner", script.getDeploymentOwner());
          reducedList.add(map);
        }
      });

      output = jsonResponse.getResponse(parentNode.putPOJO("results", reducedList));
    }
    catch (JsonProcessingException e)
    {
      e.printStackTrace();
    }

    return output;
  }

  @DeleteMapping("/destroy/id/{id}/owner/{owner}")
  public String destroy(@PathVariable("id") String id, @PathVariable("owner") String owner)
  {
    parentNode = mapper.createObjectNode();

    String response = null;

    setEnv("TF_DATA_DIR", tfDataDir);

    jsonResponse = new JSONResponse();
    try
    {
      WorkspaceUtils workspace = new WorkspaceUtils(terraform, tfDataDir);

      List<String> workspaces = workspace.list();

      String workspaceName = owner + "-" + id;

      if (workspaces.contains(workspaceName))
      {
        logger.debug("selecting existing workspace " + workspaceName);
        workspace.select(workspaceName);
      }
      else
      {
        logger.debug("creating workspace " + workspaceName);
        workspace.create(workspaceName);
      }

      response = Executor.execute(terraform + " -chdir=" + tfDataDir + " apply -destroy -auto-approve");

      output = jsonResponse.getResponse(parentNode.putPOJO("terraform", new String(Base64Utils.encode(response.getBytes()))));

      workspace.select("default");
      workspace.delete(workspaceName);
    }
    catch (IOException | InterruptedException e)
    {
      e.printStackTrace();
    }
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
