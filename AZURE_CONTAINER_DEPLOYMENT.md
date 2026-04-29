# Azure Container Deployment

This project builds as one production container:

- Spring Boot serves the REST API under `/api`
- The Vue app is built into Spring Boot static resources
- MongoDB is provided externally through environment variables

## Required Environment Variables

Set these in Azure:

```text
MONGODB_URI=<your MongoDB connection string>
MONGODB_DATABASE=ProjectPulse
PORT=8080
CORS_ALLOWED_ORIGINS=https://<your-app-hostname>
```

If the frontend and backend are served from this same container, CORS is usually not involved for normal browser use. Keeping `CORS_ALLOWED_ORIGINS` set to your Azure URL is still useful for API calls.

## Build Locally

```powershell
docker build -t project-pulse:latest .
```

## Run Locally

```powershell
docker run --rm -p 8080:8080 `
  -e MONGODB_URI="<your MongoDB connection string>" `
  -e MONGODB_DATABASE="ProjectPulse" `
  -e CORS_ALLOWED_ORIGINS="http://localhost:8080" `
  project-pulse:latest
```

Open:

```text
http://localhost:8080
```

## Azure Container App Shape

Use port `8080` as the target port. The image does not include MongoDB; use Azure Cosmos DB for MongoDB, MongoDB Atlas, or another persistent MongoDB service.
