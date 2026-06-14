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
# Le port est fourni par l'hébergeur via la variable PORT (cf. application.properties).
# -Xmx256m : tas Java plafonné à 256 Mo. Un pourcentage trop élevé faisait dépasser
# la RAM totale du conteneur (heap + metaspace + threads) -> OOM-kill et redémarrages
# en boucle. 256 Mo suffisent largement (l'app tournait déjà avec ~128 Mo).
ENTRYPOINT ["java", "-Xmx256m", "-jar", "app.jar"]
