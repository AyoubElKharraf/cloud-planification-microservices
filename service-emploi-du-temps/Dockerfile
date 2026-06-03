# Stage 1 — BUILD
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2 — RUN
FROM quay.io/wildfly/wildfly:27.0.1.Final-jdk17

USER root
RUN mkdir -p /opt/jboss/wildfly/modules/com/mysql/main
COPY --from=build /root/.m2/repository/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar \
    /opt/jboss/wildfly/modules/com/mysql/main/mysql-connector-j.jar
COPY docker/module.xml /opt/jboss/wildfly/modules/com/mysql/main/module.xml
COPY docker/entrypoint.sh /opt/jboss/entrypoint.sh
RUN sed -i 's/\r$//' /opt/jboss/entrypoint.sh \
    && chmod +x /opt/jboss/entrypoint.sh \
    && chown -R jboss:jboss /opt/jboss/wildfly/modules/com /opt/jboss/entrypoint.sh

USER jboss
COPY --from=build /app/target/service-emploi-du-temps.war $JBOSS_HOME/standalone/deployments/

ENV DATASOURCE_NAME=EmploiDS
ENV CATALOGUE_BASE_URL=http://service-catalogue:8080
ENV LOCAUX_BASE_URL=http://service-locaux:8080
EXPOSE 8080

ENTRYPOINT ["/bin/bash", "/opt/jboss/entrypoint.sh"]
