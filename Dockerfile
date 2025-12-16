FROM gradle:8.4-jdk17 AS builder
WORKDIR /home/gradle/project

COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle.kts build.gradle.kts gradle.properties* ./

COPY src ./src

RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre
WORKDIR /app
ENV JAVA_OPTS=""

RUN mkdir -p /app/uploads/images

COPY --from=builder /home/gradle/project/build/libs/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
