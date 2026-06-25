# Stage 1: Build frontend
FROM node:18 AS frontend-build
WORKDIR /app
COPY app_ui/package.json app_ui/pnpm-lock.yaml ./
RUN npm install -g pnpm && pnpm install
COPY app_ui/ .
RUN pnpm build

# Stage 2: Build backend
FROM maven:3.9-eclipse-temurin-17 AS backend-build
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY --from=frontend-build /app/dist ./src/main/resources/static/
RUN mvn clean package -DskipTests -B

# Stage 3: Runtime
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar
EXPOSE ${PORT:-9090}
ENTRYPOINT ["java", "-jar", "app.jar"]
