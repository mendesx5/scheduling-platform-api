FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline
COPY src src
RUN ./mvnw -X -DskipTests package > /tmp/build.log 2>&1; \
    status=$?; \
    echo "=== LOMBOK ==="; \
    grep -i lombok /tmp/build.log || echo "(nenhuma linha com 'lombok')"; \
    echo "=== PROCESSOR / JAVAC ARGS ==="; \
    grep -n -i "annotationProcessor\|processorpath\|proc:none\|proc:only\|Command line options" /tmp/build.log || echo "(nenhuma)"; \
    echo "=== FIM DO LOG COMPLETO ==="; \
    tail -c 4000 /tmp/build.log; \
    exit $status
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
USER app
COPY --from=build /app/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
