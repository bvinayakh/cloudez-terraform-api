package com.cez.api.v1.terraform;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.concurrent.CompletableFuture;
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
import com.cez.api.v1.terraform.request.JSONOM;
import com.cez.api.v1.terraform.request.JSONRequest;
import com.cez.api.v1.terraform.request.JSONResponse;
import com.cez.api.v1.terraform.request.RequestObject;
import com.cez.api.v1.terraform.utils.Executor;
import com.cez.api.v1.terraform.utils.WorkspaceUtils;
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
  private TerraformScriptRepository dbRepo;

  private String output;

  private JSONOM mapper = null;
  private ObjectNode parentNode = null;
  private JSONRequest jsonRequest = null;
  private JSONResponse jsonResponse = null;

  @GetMapping("/script")
  public String scriptList(@RequestParam String owner)
  {
    String response = null;
    mapper = new JSONOM();
    ObjectNode parentNode = mapper.createObjectNode();
    List<TerraformScript> scriptsList = dbRepo.findAll();
    Iterator<TerraformScript> iterator = scriptsList.iterator();
    ArrayNode scripts = mapper.createArrayNode();
    while (iterator.hasNext())
    {
      TerraformScript script = iterator.next();
      if (script.getDeploymentOwner().equalsIgnoreCase(owner));
      {
        ObjectNode content = mapper.createObjectNode();
        content.putPOJO("encoded-script", script.getTfScript());
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
    if (dbRepo.findAllById(Collections.singleton(jsonRequest.getDeploymentName())).size() < 1)
    {
      TerraformScript script = new TerraformScript();
      script.setTfScript(request.getEncodedScript());
      script.setAccount(jsonRequest.getAccount());
      script.setRegion(jsonRequest.getRegion());
      script.setDeploymentName(jsonRequest.getDeploymentName());
      script.setDeploymentDescription(jsonRequest.getDeploymentDescription());
      script.setDeploymentOwner(jsonRequest.getDeploymentOwner());
      script.setWorkspace(jsonRequest.getDeploymentName() + "-" + jsonRequest.getDeploymentOwner());
      script.setDeploymentStatus("not-executed");
      dbRepo.save(script);
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
    TerraformScript script = null;
    List<TerraformScript> list = dbRepo.findAllById(Collections.singleton(jsonRequest.getDeploymentName()));
    Iterator<TerraformScript> iterator = list.iterator();
    while (iterator.hasNext())
    {
      script = iterator.next();
      script.setTfScript(request.getEncodedScript());
      script.setAccount(jsonRequest.getAccount());
      script.setRegion(jsonRequest.getRegion());
      script.setDeploymentDescription(jsonRequest.getDeploymentDescription());
      dbRepo.save(script);
      response = jsonRequest.getDeploymentName() + " updated";
    }
    return response;
  }

  @DeleteMapping("/script")
  public String scriptDelete(@RequestParam String id)
  {
    String response = id + " not found";
    List<TerraformScript> list = dbRepo.findAllById(Collections.singleton(id));
    Iterator<TerraformScript> iterator = list.iterator();
    while (iterator.hasNext())
    {
      TerraformScript script = iterator.next();
      dbRepo.delete(script);
      response = "deleted";
    }

    return response;
  }

  @PostMapping("/execute")
  // public String execute(@RequestBody String requestBody)
  public @ResponseBody String execute(@RequestBody String requestBody)
  {
    mapper = new JSONOM();
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
      Optional<TerraformScript> optionalScript = dbRepo.findById(request.getId());
      if (optionalScript.isPresent())
      {
        TerraformScript script = optionalScript.get();

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

  @PostMapping("/test")
  public @ResponseBody String test(@RequestBody String requestBody)
  {
    return "test";
  }

  @DeleteMapping("/destroy/id/{id}/owner/{owner}")
  public String destroy(@PathVariable("id") String id, @PathVariable("owner") String owner)
  {
    mapper = new JSONOM();
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
