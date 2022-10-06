FROM openjdk:8-jdk-alpine

# Download terraform for linux
RUN wget https://releases.hashicorp.com/terraform/1.2.9/terraform_1.2.9_linux_amd64.zip

# Unzip
RUN \
		unzip terraform_1.2.9_linux_amd64.zip && \
		rm -rf terraform_1.2.9_linux_amd64 && \
		mv terraform /usr/bin/terraform && \
		chmod +x /usr/bin/terraform && \
		terraform --version

COPY ["terraform-resources","./terraform-resources"]
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]