package com.cez.terraform.api.v1;

import java.util.Date;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.springframework.util.Base64Utils;
import com.cez.terraform.api.v1.utils.TimeUtil;

@Entity
@Indexed
@Table(name = "terraform_deployments")
public class Scripts
{
  private @Column(name = "tf_script", nullable = false) String tfScript;
  private @Column(name = "account", nullable = false) String account;
  private @Id @Column(name = "deployment_name", nullable = false) @FullTextField String deploymentName;
  private @Column(name = "deployment_description", nullable = true) @FullTextField String deploymentDescription;
  private @Column(name = "deployment_owner", nullable = false) String deploymentOwner;
  private @Column(name = "region", nullable = true) String region;
  private @Column(name = "deployment_status", nullable = true) String deploymentStatus;
  private @Column(name = "created_on", nullable = false) Date createdOn;
  private @Column(name = "workspace", nullable = false) String workspace;

  public Scripts()
  {
    this.createdOn = TimeUtil.getCurrentUTCDateWithTimeZone();
  }
  
  public String getEncodedScript()
  {
    return tfScript;
  }

  public String getTfScript()
  {
    // return tfScript;
    return new String(Base64Utils.decodeFromString(tfScript));
  }

  public void setTfScript(String tfScript)
  {
    this.tfScript = tfScript;
  }

  public String getAccount()
  {
    return account;
  }

  public void setAccount(String account)
  {
    this.account = account;
  }

  public String getDeploymentName()
  {
    return deploymentName;
  }

  public void setDeploymentName(String deploymentName)
  {
    this.deploymentName = deploymentName;
  }

  public String getDeploymentDescription()
  {
    return deploymentDescription;
  }

  public void setDeploymentDescription(String deploymentDescription)
  {
    this.deploymentDescription = deploymentDescription;
  }

  public String getDeploymentOwner()
  {
    return deploymentOwner;
  }

  public void setDeploymentOwner(String deploymentOwner)
  {
    this.deploymentOwner = deploymentOwner;
  }

  public String getRegion()
  {
    return region;
  }

  public void setRegion(String region)
  {
    this.region = region;
  }

  public void setDeploymentStatus(String status)
  {
    this.deploymentStatus = status;
  }

  public String getDeploymentStatus()
  {
    return deploymentStatus;
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

  // defaults
  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (!(obj instanceof Scripts)) return false;
    Scripts asset = (Scripts) obj;
    return Objects.equals(this.deploymentName, asset.deploymentName) && Objects.equals(this.account, asset.account);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(this.account, this.deploymentName);
  }

  @Override
  public String toString()
  {
    return this.deploymentName;
  }

}
