FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline
COPY src src
RUN ./mvnw -X -DskipTests package > /tmp/build.log 2>&1; \
    status=$?; \
    echo "=== BUILD STATUS: $status ==="; \
    echo "=== LOMBOK NA LISTA DE DEPENDENCIAS RESOLVIDAS ==="; \
    grep -n -i "lombok.*jar\|Included.*lombok\|Downloaded.*lombok\|Downloading.*lombok" /tmp/build.log | head -20 || echo "(nenhuma)"; \
    echo "=== ARGUMENTOS PASSADOS AO JAVAC ==="; \
    grep -n -A2 -i "Command line options\|javac.*-processorpath\|-processorpath" /tmp/build.log | head -40 || echo "(nenhuma)"; \
    echo "=== TODAS AS LINHAS COM 'lombok' (grep final, deve ficar visivel) ==="; \
    grep -c -i lombok /tmp/build.log; \
    grep -i lombok /tmp/build.log | head -80; \
    echo "=== FIM ==="; \
    exit $status
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
USER app
COPY --from=build /app/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
