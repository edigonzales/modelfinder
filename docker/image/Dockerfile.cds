#FROM bellsoft/liberica-openjdk-debian:21.0.7-9-cds AS builder
FROM bellsoft/liberica-openjdk-debian:24.0.1-11-cds AS builder

WORKDIR /workspace/app

COPY gradlew .
COPY gradle gradle

COPY build.gradle .
COPY settings.gradle .

COPY src src

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew build -x test

RUN pwd
RUN ls -la build/libs


#FROM bellsoft/liberica-openjdk-debian:21.0.7-9-cds AS optimizer
FROM bellsoft/liberica-openjdk-debian:24.0.1-11-cds AS optimizer


WORKDIR /app
COPY --from=builder /workspace/app/build/libs/modelfinder-*-exec.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --destination extracted
RUN pwd
RUN ls -la


#FROM bellsoft/liberica-openjdk-debian:21.0.7-9-cds 
FROM bellsoft/liberica-openjdk-debian:24.0.1-11-cds

ARG UID=1001
RUN adduser -u $UID modelfinder 

WORKDIR /work
RUN chown $UID:0 . && \
    chmod 0775 . && \
    ls -la
VOLUME ["/work"]

ENV HOME=/app
WORKDIR /app

RUN chown $UID:0 . && \
    chmod 0775 . && \
    ls -la

USER $UID

ENTRYPOINT ["java", "-Dspring.aot.enabled=true", "-XX:SharedArchiveFile=application.jsa", "-jar", "/app/app.jar", "--spring.profiles.active=docker"]
#ENTRYPOINT ["java", "-Dspring.aot.enabled=true", "-XX:AOTCache=app.aot", "-jar", "/app/app.jar", "--spring.profiles.active=docker"]

WORKDIR /app
COPY --chown=$UID:0 --chmod=0775 --from=optimizer /app/extracted/dependencies/ ./
COPY --chown=$UID:0 --chmod=0775 --from=optimizer /app/extracted/spring-boot-loader/ ./
COPY --chown=$UID:0 --chmod=0775 --from=optimizer /app/extracted/snapshot-dependencies/ ./
COPY --chown=$UID:0 --chmod=0775 --from=optimizer /app/extracted/application/ ./

USER $UID

RUN java -Dspring.aot.enabled=true -XX:ArchiveClassesAtExit=./application.jsa -Dspring.context.exit=onRefresh -jar /app/app.jar --spring.profiles.active=docker
#RUN java -Dspring.aot.enabled=true -XX:AOTMode=record -XX:AOTConfiguration=app.aotconf -Dspring.context.exit=onRefresh -jar /app/app.jar --spring.profiles.active=docker
#RUN java -Dspring.aot.enabled=true -XX:AOTMode=create -XX:AOTConfiguration=app.aotconf -XX:AOTCache=app.aot -jar /app/app.jar --spring.profiles.active=docker
