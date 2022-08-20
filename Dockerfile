#FROM openjdk:18-jdk-alpine as build
FROM gradle:jdk18-alpine as build

#RUN apk add gradle

WORKDIR /tpm-tools-backend
#COPY build.gradle settings.gradle ./
#COPY src src/
COPY . ./
RUN gradle build


FROM openjdk:18-jdk-alpine as runner
COPY --from=build /tpm-tools-backend/build/libs/tpm-tools-backend-0.0.1-SNAPSHOT.jar app.jar

CMD ["java", "-jar", "app.jar"]

#docker build . -t chris/tpm-tools-backend
#docker run -d -p 8080:8080  --name tpm-tools-backend chris/tpm-tools-backend








