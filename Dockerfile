FROM maven:3-amazoncorretto-17 as builder

ADD ./pom.xml pom.xml
ADD ./src src/

#RUN mvn clean package -Dmaven.test.skip=true

RUN --mount=type=cache,target=/root/.m2,rw mvn clean package -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true


FROM amazoncorretto:17-alpine-jdk 

# Install curl for tests
RUN apk --no-cache add curl apache2-utils

# copy jar from builder stage
COPY --from=builder target/*.jar app.jar

EXPOSE 8080 8080

CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar", "-Xms64m"]