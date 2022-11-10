package com.cez.terraform.api.v1.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkspaceUtils
{
  public static final Logger logger = LoggerFactory.getLogger(WorkspaceUtils.class);

  private String tfDataDir = null;
  private String terraformExec = null;

  public WorkspaceUtils(String terraformExec, String tfDataDir) throws IOException, InterruptedException
  {
    this.terraformExec = terraformExec;
    this.tfDataDir = tfDataDir;
    init();
  }

  public void create(String workspaceName) throws IOException, InterruptedException
  {
    logger.debug(Executor.execute(terraformExec + " -chdir=" + tfDataDir + "  workspace new " + workspaceName));
  }

  public void select(String workspaceName) throws IOException, InterruptedException
  {
    logger.debug(Executor.execute(terraformExec + " -chdir=" + tfDataDir + "  workspace select " + workspaceName));
  }


  public void delete(String workspaceName) throws IOException, InterruptedException
  {
    logger.debug(Executor.execute(terraformExec + " -chdir=" + tfDataDir + "  workspace delete " + workspaceName));
  }

  public List<String> list() throws IOException, InterruptedException
  {
    List<String> workspaces = new ArrayList<>();
    String output = Executor.execute(terraformExec + " -chdir=" + tfDataDir + "  workspace list");
    StringTokenizer tokenizer = new StringTokenizer(output);
    while (tokenizer.hasMoreTokens())
    {
      workspaces.add(tokenizer.nextToken());
    }
    return workspaces;
  }

  private void init() throws IOException, InterruptedException
  {
    File backendTF = new File(tfDataDir + "/backend.tf");
    org.apache.commons.io.FileUtils.copyFile(new File("terraform-resources/backend.tf"), backendTF);
    logger.debug(Executor.execute(terraformExec + " -chdir=" + tfDataDir + " init"));
  }
}
