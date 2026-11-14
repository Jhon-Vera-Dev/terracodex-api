# syntax=docker/dockerfile:1
# terracodex-api: imagen para Dokploy (o cualquier servicio de contenedores).
# Las pruebas no se corren aquí: se corren antes de desplegar (./mvnw test).

FROM eclipse-temurin:21-jdk AS compilacion
WORKDIR /fuente
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline
COPY src src
RUN ./mvnw -q -B -DskipTests package \
 && java -Djarmode=tools -jar target/terracodex-api-*.jar extract --layers --launcher --destination /extraido

FROM eclipse-temurin:21-jre
WORKDIR /app
# Capas: dependencias primero (cambian poco) y el código al final, para que los redespliegues suban menos
COPY --from=compilacion /extraido/dependencies/ ./
COPY --from=compilacion /extraido/spring-boot-loader/ ./
COPY --from=compilacion /extraido/snapshot-dependencies/ ./
COPY --from=compilacion /extraido/application/ ./
# Sin root dentro del contenedor
RUN useradd --system --uid 10001 app
USER app
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError -Duser.timezone=America/Lima"
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD bash -c 'exec 3<>/dev/tcp/127.0.0.1/${PORT:-8080} && printf "GET /actuator/health HTTP/1.0\r\n\r\n" >&3 && grep -q "UP" <&3'
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
