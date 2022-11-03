package com.cez.api.v1.terraform.async;

import java.util.Date;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import com.cez.api.v1.terraform.utils.TimeUtil;

@Entity
@Table(name = "terraform_executions")
public class TerraformExecutions
{
  private @Id @Column(name = "id", nullable = false) String id;
  private @Column(name = "status", nullable = true) String status;
  private @Column(name = "console_output", nullable = true) String consoleOutput;
  private @Column(name = "script_id", nullable = false) String scriptId;
  private @Column(name = "executed_by", nullable = false) String executedBy;
  private @Column(name = "workspace", nullable = false) String workspace;
  private @Column(name = "logical_resources", nullable = true) String logicalResources;
  private @Column(name = "execution_output", nullable = true) String executionOutput;
  private @Column(name = "created_on", nullable = true) Date createdOn;
  private @Column(name = "execution_response", nullable = true) String executionResponse;

  public TerraformExecutions()
  {
    this.createdOn = TimeUtil.getCurrentUTCDateWithTimeZone();
  }


  public Date getCreatedOn()
  {
    return createdOn;
  }

  public String getWorkspace()
  {
    return workspace;
  }

  public void setWorkspace(String workspace)
  {
    this.workspace = workspace;
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }


  public void setStatus(String status)
  {
    this.status = status;
  }


  public void setConsoleOutput(String consoleOutput)
  {
    this.consoleOutput = consoleOutput;
  }


  public void setScriptId(String scriptId)
  {
    this.scriptId = scriptId;
  }


  public void setExecutedBy(String executedBy)
  {
    this.executedBy = executedBy;
  }


  public void setLogicalResources(String logicalResources)
  {
    this.logicalResources = logicalResources;
  }


  public void setExecutionOutput(String executionOutput)
  {
    this.executionOutput = executionOutput;
  }


  public String getStatus()
  {
    return status;
  }


  public String getConsoleOutput()
  {
    return consoleOutput;
  }


  public String getScriptId()
  {
    return scriptId;
  }


  public String getExecutedBy()
  {
    return executedBy;
  }


  public String getExecutionResponse()
  {
    return executionResponse;
  }


  public void setExecutionResponse(String executionResponse)
  {
    this.executionResponse = executionResponse;
  }


  public String getLogicalResources()
  {
    return logicalResources;
  }


  public String getExecutionOutput()
  {
    return executionOutput;
  }


  // defaults
  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (!(obj instanceof TerraformExecutions)) return false;
    TerraformExecutions asset = (TerraformExecutions) obj;
    return Objects.equals(this.id, asset.id) && Objects.equals(this.executedBy, asset.executedBy);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(this.id, this.executedBy);
  }

  @Override
  public String toString()
  {
    return this.id;
  }

}
