FROM --platform=linux/arm64/v8 gradle:jdk18 as build

WORKDIR /tpm-tools-backend
COPY . ./
RUN gradle build -x test


FROM --platform=linux/amd64 openjdk:18-jdk-alpine as runner
COPY --from=build /tpm-tools-backend/build/libs/tpm-tools-backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]

#docker build . -t chris/tpm-tools-backend
#docker run -d -p 8080:8080  --name tpm-tools-backend chris/tpm-tools-backend








