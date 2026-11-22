# syntax=docker/dockerfile:1
# Build từ thư mục gốc repo vì service phụ thuộc module common:
#   docker build -f monitoring-service/Dockerfile -t monitoring-service .

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Cài common vào kho Maven trước để service resolve được dependency com.example:common.
COPY common/pom.xml common/pom.xml
COPY common/src common/src
RUN --mount=type=cache,target=/root/.m2 mvn -q -B -f common/pom.xml -DskipTests install

COPY monitoring-service/pom.xml monitoring-service/pom.xml
COPY monitoring-service/src monitoring-service/src
# Test cần MySQL, Kafka… nên không chạy lúc build image.
RUN --mount=type=cache,target=/root/.m2 mvn -q -B -f monitoring-service/pom.xml -DskipTests package \
    && cp "$(find monitoring-service/target -maxdepth 1 -name '*.jar' ! -name '*-plain.jar' | head -n 1)" app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system spring && useradd --system --gid spring spring
COPY --from=build --chown=spring:spring /workspace/app.jar app.jar
USER spring

ENV SPRING_PROFILES_ACTIVE=docker
# Heap theo giới hạn bộ nhớ của container; mặc định JVM chỉ lấy 25% nên dễ thiếu heap.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
