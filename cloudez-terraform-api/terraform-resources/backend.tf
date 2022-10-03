#-----------------------
# Backend Configuration
#-----------------------

terraform {
  backend "s3" {
    bucket         = "s3usw2-dataplatform-sbx-tfstate"
    key            = "terraform/dataez/us-west-2/infra.tfstate"
    region         = "us-west-2"
    encrypt        = true
    dynamodb_table = "terraform_locks"

  }

}