#!/usr/bin/env bash
set -euo pipefail

SOURCE_DIR="${DEPLOYMENT_SOURCE:-$(pwd)}"
TARGET_DIR="${DEPLOYMENT_TARGET:-/home/site/wwwroot}"

echo "Building Project Pulse from ${SOURCE_DIR}"

cd "${SOURCE_DIR}/frontend"
npm ci
VITE_API_BASE_URL=/api npm run build

cd "${SOURCE_DIR}/backend"
mkdir -p src/main/resources/static
rm -rf src/main/resources/static/*
cp -R "${SOURCE_DIR}/frontend/dist/." src/main/resources/static/
mvn -DskipTests package

mkdir -p "${TARGET_DIR}"
cp target/project-pulse-backend-*.jar "${TARGET_DIR}/app.jar"

echo "Project Pulse deployed to ${TARGET_DIR}/app.jar"
