# syntax=docker/dockerfile:1

FROM node:20-alpine AS frontend-build
WORKDIR /app/frontend

COPY frontend/package*.json ./
RUN npm ci

COPY frontend/ ./
ARG VITE_API_BASE_URL=/api
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
RUN npm run build

FROM maven:3.9.9-eclipse-temurin-17 AS backend-build
WORKDIR /app

COPY backend/pom.xml backend/pom.xml
RUN mvn -f backend/pom.xml dependency:go-offline

COPY backend/ backend/
COPY --from=frontend-build /app/frontend/dist/ backend/src/main/resources/static/
RUN mvn -f backend/pom.xml -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S projectpulse && adduser -S projectpulse -G projectpulse
COPY --from=backend-build /app/backend/target/project-pulse-backend-*.jar /app/app.jar

ENV PORT=8080
EXPOSE 8080

USER projectpulse
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
