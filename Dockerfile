FROM gradle:8.14.3-jdk17 AS builder
WORKDIR /home/gradle/project

COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle.kts build.gradle.kts gradle.properties* ./

RUN chmod +x ./gradlew
RUN gradle dependencies --no-daemon --console=plain

COPY src ./src

RUN gradle bootJar --no-daemon --console=plain --info -x test

FROM eclipse-temurin:17-jre
WORKDIR /app
ENV JAVA_OPTS=""

RUN mkdir -p /app/uploads/images

COPY --from=builder /home/gradle/project/build/libs/*.jar /app/app.jar
COPY uploads ./uploads

EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
