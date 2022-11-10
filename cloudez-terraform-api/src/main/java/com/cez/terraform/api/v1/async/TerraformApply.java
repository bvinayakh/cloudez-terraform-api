package com.cez.terraform.api.v1.async;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import com.cez.terraform.api.v1.Deployments;
import com.cez.terraform.api.v1.DeploymentsRepository;
import com.cez.terraform.api.v1.Scripts;
import com.cez.terraform.api.v1.ScriptsRepository;
import com.cez.terraform.api.v1.request.JSONOM;
import com.cez.terraform.api.v1.request.JSONRequest;
import com.cez.terraform.api.v1.request.JSONResponse;
import com.cez.terraform.api.v1.request.RequestObject;
import com.cez.terraform.api.v1.utils.Executor;
import com.cez.terraform.api.v1.utils.WorkspaceUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class TerraformApply implements Runnable
{
  public static final Logger logger = LoggerFactory.getLogger(TerraformApply.class);

  private String tfDataDir = null;
  private String requestBody = null;
  private String terraform = null;
  private String taskId = null;

  private ScriptsRepository dbRepo = null;
  private DeploymentsRepository executionRepo = null;


  public TerraformApply(String requestBody, String tfDataDir, String terraform, String taskId, ScriptsRepository dbRepo,
      DeploymentsRepository executionRepo)
  {
    this.requestBody = requestBody;
    this.tfDataDir = tfDataDir;
    this.terraform = terraform;
    this.taskId = taskId;

    this.dbRepo = dbRepo;
    this.executionRepo = executionRepo;
  }

  @Override
  public void run()
  {
    JSONOM mapper = new JSONOM();
    ObjectNode parentNode = mapper.createObjectNode();
    ObjectNode section = mapper.createObjectNode();

    String response = null;

    Deployments execution = new Deployments();

    List<String> logicalResources = new ArrayList<>();

    JSONRequest jsonRequest = new JSONRequest(requestBody);
    JSONResponse jsonResponse = new JSONResponse();
    RequestObject request = (RequestObject) jsonRequest.getContent();
    try
    {
      execution.setId(taskId);

      Optional<Scripts> optionalScript = dbRepo.findById(request.getId());
      if (optionalScript.isPresent())
      {
        Scripts script = optionalScript.get();

        execution.setScriptId(script.getDeploymentName());
        execution.setExecutedBy(script.getDeploymentOwner());

        WorkspaceUtils workspace = new WorkspaceUtils(terraform, tfDataDir);
        List<String> workspaces = workspace.list();
        if (workspaces.contains(script.getWorkspace()))
        {
          logger.debug("selecting existing workspace " + script.getWorkspace());
          workspace.select(script.getWorkspace());
          execution.setWorkspace(script.getWorkspace());
        }
        else
        {
          logger.debug("creating workspace " + script.getWorkspace());
          workspace.create(script.getWorkspace());
          execution.setWorkspace(script.getWorkspace());
        }

        File mainTF = new File(tfDataDir + "/main.tf");
        FileWriter fileWriter = new FileWriter(mainTF);
        fileWriter.write(script.getTfScript());
        fileWriter.close();
        if (mainTF.exists())
        {
          response = Executor.execute(terraform + " -chdir=" + tfDataDir + " apply -auto-approve");
          System.out.println(response);
          execution.setConsoleOutput(response);

          String out = Executor.execute(terraform + " -chdir=" + tfDataDir + " state list");
          StringTokenizer tokenizer = new StringTokenizer(out);
          while (tokenizer.hasMoreTokens())
          {
            logicalResources.add(tokenizer.nextToken());
          }
        }
        section.putPOJO("output", new String(Base64Utils.encode(response.getBytes())));
        section.putPOJO("logical-resource-name", logicalResources);
        execution.setExecutionOutput(section.asText());
        execution.setLogicalResources(StringUtils.collectionToCommaDelimitedString(logicalResources));
        execution.setStatus("executed");
        execution.setExecutionResponse(jsonResponse.getResponse(parentNode.putPOJO("terraform", section)));

        // System.out.println(jsonResponse.getResponse(parentNode.putPOJO("terraform", section)));
      }
    }
    catch (IOException | InterruptedException e)
    {
      execution.setStatus("error");
      e.printStackTrace();
    }

    executionRepo.save(execution);
  }
}
