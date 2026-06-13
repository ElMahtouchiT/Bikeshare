# ===== Étape 1 : build du jar avec Maven =====
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -B

# ===== Étape 2 : image d'exécution légère =====
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Le port est fourni par l'hébergeur via la variable PORT (cf. application.properties)
ENTRYPOINT ["java", "-jar", "app.jar"]
