#FROM openjdk:8-jdk-alpine
FROM amazoncorretto

RUN yum -y install wget

# Download terraform for linux
RUN wget https://releases.hashicorp.com/terraform/1.2.9/terraform_1.2.9_linux_amd64.zip

# Unzip
RUN unzip terraform_1.2.9_linux_amd64.zip
RUN rm -rf terraform_1.2.9_linux_amd64
RUN mv terraform /usr/bin/terraform
RUN	/usr/bin/terraform --version 

COPY ["terraform-resources","./terraform-resources"]
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]