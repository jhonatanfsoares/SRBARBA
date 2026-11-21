# Etapa 1: compila o projeto
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# Etapa 2: imagem leve só com o Java para rodar
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/srbarba-1.0.0.jar app.jar
ENV TZ=America/Sao_Paulo
EXPOSE 8080
CMD ["sh", "-c", "java -Xmx350m -jar app.jar"]
