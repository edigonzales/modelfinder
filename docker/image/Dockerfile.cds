FROM bellsoft/liberica-openjdk-debian:21.0.7-9-cds AS builder

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


FROM bellsoft/liberica-openjdk-debian:21.0.7-9-cds as optimizer

WORKDIR /app
COPY --from=builder /workspace/app/build/libs/modelfinder-*-exec.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --destination extracted
RUN pwd
RUN ls -la


FROM bellsoft/liberica-openjdk-debian:21.0.7-9-cds 

ARG UID=1001
RUN adduser -u $UID modelfinder 

ENV HOME=/app
WORKDIR /app

RUN chown $UID:0 . && \
    chmod 0775 . && \
    ls -la

ENTRYPOINT ["java", "-Dspring.aot.enabled=true", "-XX:SharedArchiveFile=application.jsa", "-jar", "/app/app.jar"]

WORKDIR /app
COPY --chown=$UID:0 --chmod=0775 --from=optimizer /app/extracted/dependencies/ ./
COPY --chown=$UID:0 --chmod=0775 --from=optimizer /app/extracted/spring-boot-loader/ ./
COPY --chown=$UID:0 --chmod=0775 --from=optimizer /app/extracted/snapshot-dependencies/ ./
COPY --chown=$UID:0 --chmod=0775 --from=optimizer /app/extracted/application/ ./

USER $UID

RUN java -Dspring.aot.enabled=true -XX:ArchiveClassesAtExit=./application.jsa -Dspring.context.exit=onRefresh -jar /app/app.jar